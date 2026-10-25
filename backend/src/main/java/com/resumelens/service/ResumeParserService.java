package com.resumelens.service;

import com.resumelens.exception.ResumeParseException;
import com.resumelens.exception.UnsupportedFileTypeException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    private static final Pattern SECTION_HEADER = Pattern.compile(
        "(?i)^(EDUCATION|EXPERIENCE|SKILLS|PROJECTS|CERTIFICATIONS|SUMMARY|OBJECTIVE|WORK EXPERIENCE|PROFESSIONAL EXPERIENCE)[:\\s]*$",
        Pattern.MULTILINE
    );

    private static final Pattern EMAIL_PATTERN  = Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN  = Pattern.compile("[+]?[0-9][\\s\\-.]?[(]?[0-9]{3}[)]?[\\s\\-.]?[0-9]{3}[\\s\\-.]?[0-9]{4,}");

    /** Extracts raw text from PDF or DOCX; throws typed exceptions for parse or type failures. */
    public String extractText(MultipartFile file) {
        String name = Objects.requireNonNull(file.getOriginalFilename(), "Filename must not be null");
        int lastDot = name.lastIndexOf('.');
        String ext  = lastDot >= 0 ? name.substring(lastDot + 1).toLowerCase(Locale.ROOT) : "";
        return switch (ext) {
            case "pdf"  -> extractPdf(file);
            case "docx" -> extractDocx(file);
            default     -> throw new UnsupportedFileTypeException(ext);
        };
    }

    /**
     * Detects embedded graphics or images in uploaded resumes:
     * - For PDF: any page containing an image XObject
     * - For DOCX: doc.getAllPictures() is not empty
     */
    public boolean hasGraphics(MultipartFile file) {
        String name = Objects.requireNonNull(file.getOriginalFilename(), "Filename must not be null");
        int lastDot = name.lastIndexOf('.');
        String ext  = lastDot >= 0 ? name.substring(lastDot + 1).toLowerCase(Locale.ROOT) : "";
        return switch (ext) {
            case "pdf"  -> hasPdfGraphics(file);
            case "docx" -> hasDocxGraphics(file);
            default     -> false;
        };
    }

    private boolean hasPdfGraphics(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            if (!hasPdfSignature(bytes)) {
                return false;
            }
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                for (PDPage page : doc.getPages()) {
                    PDResources resources = page.getResources();
                    if (resources != null) {
                        for (COSName name : resources.getXObjectNames()) {
                            if (resources.isImageXObject(name)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private boolean hasDocxGraphics(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            if (!hasDocxSignature(bytes)) {
                return false;
            }
            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                 XWPFDocument doc = new XWPFDocument(in)) {
                return !doc.getAllPictures().isEmpty();
            }
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private String extractPdf(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            if (!hasPdfSignature(bytes)) {
                throw new UnsupportedFileTypeException("pdf");
            }
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                String text = new PDFTextStripper().getText(doc).trim();
                if (text.isBlank()) {
                    throw new ResumeParseException(
                        "PDF has no text layer (scanned or image-only). Please upload a text-selectable PDF.", null);
                }
                return text;
            }
        } catch (UnsupportedFileTypeException | ResumeParseException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new ResumeParseException("Failed to read PDF: " + e.getMessage(), e);
        }
    }

    private String extractDocx(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            if (!hasDocxSignature(bytes)) {
                throw new UnsupportedFileTypeException("docx");
            }
            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                 XWPFDocument doc = new XWPFDocument(in)) {
                StringBuilder sb = new StringBuilder();
                doc.getParagraphs().forEach(p -> sb.append(p.getText()).append('\n'));
                return sb.toString().trim();
            }
        } catch (UnsupportedFileTypeException | ResumeParseException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new ResumeParseException("Failed to read DOCX: " + e.getMessage(), e);
        }
    }

    private static boolean hasPdfSignature(byte[] bytes) {
        if (bytes == null || bytes.length < 5) {
            return false;
        }
        return bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-';
    }

    private static boolean hasDocxSignature(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return false;
        }
        return bytes[0] == 'P' && bytes[1] == 'K' && bytes[2] == 0x03 && bytes[3] == 0x04;
    }

    /**
     * Splits raw text into named sections using regex header detection.
     * Returns a map of section name -> section body text.
     */
    public Map<String, String> extractSections(String rawText) {
        Map<String, String> sections = new LinkedHashMap<>();
        String[] lines = rawText.split("\n");
        String currentSection = "HEADER";
        StringBuilder body = new StringBuilder();

        for (String line : lines) {
            if (SECTION_HEADER.matcher(line.trim()).matches()) {
                sections.put(currentSection, body.toString().trim());
                currentSection = line.trim().toUpperCase(Locale.ROOT).replaceAll("[:\\s]+$", "");
                body = new StringBuilder();
            } else {
                body.append(line).append('\n');
            }
        }
        sections.put(currentSection, body.toString().trim());
        return sections;
    }

    /**
     * Heuristic check for multi-column or table-like formatting.
     * Detects when text tokens are distributed abnormally across a line
     * (multiple long whitespace gaps suggest columns were merged by PDFBox out of reading order).
     * This is a heuristic with a known false-positive rate.
     * Upgrade path: use PDFBox layout analysis API for true column detection.
     */
    public boolean detectFormattingRisks(String rawText) {
        long suspiciousLines = Arrays.stream(rawText.split("\n"))
            .filter(line -> line.matches(".*\\s{4,}.*\\s{4,}.*"))  // two or more 4+ space gaps
            .count();
        long totalLines = rawText.split("\n").length;
        // If more than 20% of lines look multi-column, flag it
        return totalLines > 0 && (double) suspiciousLines / totalLines > 0.20;
    }

    public boolean hasContactInfo(String rawText) {
        return EMAIL_PATTERN.matcher(rawText).find() || PHONE_PATTERN.matcher(rawText).find();
    }
}