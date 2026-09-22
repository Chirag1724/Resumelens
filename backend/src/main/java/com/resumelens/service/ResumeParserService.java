package com.resumelens.service;

import com.resumelens.exception.ResumeParseException;
import com.resumelens.exception.UnsupportedFileTypeException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        String ext  = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "pdf"  -> extractPdf(file);
            case "docx" -> extractDocx(file);
            default     -> throw new UnsupportedFileTypeException(ext);
        };
    }

    private String extractPdf(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                String text = new PDFTextStripper().getText(doc).trim();
                if (text.isBlank()) {
                    throw new ResumeParseException(
                        "PDF has no text layer (scanned or image-only). Please upload a text-selectable PDF.", null);
                }
                return text;
            }
        } catch (ResumeParseException e) {
            throw e;
        } catch (IOException e) {
            throw new ResumeParseException("Failed to read PDF: " + e.getMessage(), e);
        }
    }

    private String extractDocx(MultipartFile file) {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();
            doc.getParagraphs().forEach(p -> sb.append(p.getText()).append('\n'));
            return sb.toString().trim();
        } catch (IOException e) {
            throw new ResumeParseException("Failed to read DOCX: " + e.getMessage(), e);
        }
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
                currentSection = line.trim().toUpperCase().replaceAll("[:\\s]+$", "");
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