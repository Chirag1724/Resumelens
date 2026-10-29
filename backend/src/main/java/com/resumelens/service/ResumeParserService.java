package com.resumelens.service;

import com.resumelens.exception.ResumeParseException;
import com.resumelens.exception.UnsupportedFileTypeException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.apache.pdfbox.contentstream.operator.state.Restore;
import org.apache.pdfbox.contentstream.operator.state.Save;
import org.apache.pdfbox.contentstream.operator.state.SetMatrix;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    public static final float MIN_DRAWN_POINTS = 72.0f;
    public static final double EMU_PER_INCH = 914400.0;
    public static final double POINTS_PER_INCH = 72.0;

    public static final Map<String, Set<String>> SECTION_ALIASES = Map.of(
        "EXPERIENCE", Set.of(
            "experience",
            "work experience",
            "professional experience",
            "internships",
            "employment history"
        ),
        "EDUCATION", Set.of(
            "education",
            "academic background",
            "qualifications"
        ),
        "SKILLS", Set.of(
            "skills",
            "technical skills",
            "key skills",
            "core competencies"
        ),
        "PROJECTS", Set.of(
            "projects",
            "academic projects",
            "personal projects"
        ),
        "CERTIFICATIONS", Set.of(
            "certifications",
            "courses",
            "achievements",
            "awards"
        )
    );

    private static final Map<String, String> ALIAS_TO_CANONICAL = buildAliasToCanonicalMap();

    private static Map<String, String> buildAliasToCanonicalMap() {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : SECTION_ALIASES.entrySet()) {
            for (String alias : entry.getValue()) {
                map.put(alias, entry.getKey());
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public static String normalizeHeading(String line) {
        if (line == null) {
            return "";
        }
        return line.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    public static Optional<String> findCanonicalSection(String line) {
        if (line == null) {
            return Optional.empty();
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.length() > 40 || trimmed.endsWith(".")) {
            return Optional.empty();
        }
        String[] words = trimmed.split("\\s+");
        if (words.length > 5) {
            return Optional.empty();
        }
        String normalized = normalizeHeading(trimmed);
        String canonical = ALIAS_TO_CANONICAL.get(normalized);
        return Optional.ofNullable(canonical);
    }

    private static final Pattern EMAIL_PATTERN  = Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN  = Pattern.compile("[+]?[0-9][\\s\\-.]?[(]?[0-9]{3}[)]?[\\s\\-.]?[0-9]{3}[\\s\\-.]?[0-9]{4,}");

    public record PdfImageInfo(int pixelWidth, int pixelHeight, float drawnWidthPt, float drawnHeightPt, boolean counted) {}

    public static class PdfGraphicsStreamEngine extends PDFStreamEngine {
        private final List<PdfImageInfo> images = new ArrayList<>();

        public PdfGraphicsStreamEngine() {
            addOperator(new Save(this));
            addOperator(new Restore(this));
            addOperator(new Concatenate(this));
            addOperator(new SetMatrix(this));
            addOperator(new DrawObjectProcessor(this));
        }

        public List<PdfImageInfo> getImages() {
            return images;
        }

        public boolean hasCountedGraphics() {
            for (PdfImageInfo info : images) {
                if (info != null && info.counted()) {
                    return true;
                }
            }
            return false;
        }

        private class DrawObjectProcessor extends OperatorProcessor {
            public DrawObjectProcessor(PDFStreamEngine context) {
                super(context);
            }

            @Override
            public void process(Operator operator, List<COSBase> operands) throws IOException {
                if (operands.isEmpty()) return;
                COSBase base = operands.get(0);
                if (!(base instanceof COSName name)) return;
                PDResources resources = getContext().getResources();
                if (resources == null) return;
                PDXObject xobject = resources.getXObject(name);
                if (xobject instanceof PDImageXObject img) {
                    Matrix ctm = getContext().getGraphicsState().getCurrentTransformationMatrix();
                    float drawnWidth = Math.abs(ctm.getScalingFactorX());
                    float drawnHeight = Math.abs(ctm.getScalingFactorY());
                    boolean counted = drawnWidth >= MIN_DRAWN_POINTS && drawnHeight >= MIN_DRAWN_POINTS;
                    images.add(new PdfImageInfo(img.getWidth(), img.getHeight(), drawnWidth, drawnHeight, counted));
                } else if (xobject instanceof PDFormXObject form) {
                    getContext().increaseLevel();
                    try {
                        if (getContext().getLevel() <= 50) {
                            if (form instanceof PDTransparencyGroup group) {
                                getContext().showTransparencyGroup(group);
                            } else {
                                getContext().showForm(form);
                            }
                        }
                    } finally {
                        getContext().decreaseLevel();
                    }
                }
            }

            @Override
            public String getName() {
                return "Do";
            }
        }
    }

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
     * - For PDF: process each page content stream with a PDFStreamEngine subclass and read drawn width/height in points.
     * - For DOCX: use the drawn extent of each inline or anchored picture (914400 EMU per inch).
     * Count an image only when both drawn width and drawn height are at least 72 points.
     * Vector drawings and form XObjects without an image are never counted.
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

    public List<PdfImageInfo> detectPdfImages(byte[] bytes) {
        if (!hasPdfSignature(bytes)) {
            return List.of();
        }
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PdfGraphicsStreamEngine engine = new PdfGraphicsStreamEngine();
            for (PDPage page : doc.getPages()) {
                engine.processPage(page);
            }
            return engine.getImages();
        } catch (IOException | RuntimeException e) {
            return List.of();
        }
    }

    private boolean hasPdfGraphics(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            for (PdfImageInfo info : detectPdfImages(bytes)) {
                if (info != null && info.counted()) {
                    return true;
                }
            }
            return false;
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
                return detectDocxGraphics(doc);
            }
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    public boolean detectDocxGraphics(XWPFDocument doc) {
        List<XWPFParagraph> paragraphs = new ArrayList<>(doc.getParagraphs());
        for (XWPFTable table : doc.getTables()) {
            collectTableParagraphs(table, paragraphs);
        }
        for (XWPFHeader header : doc.getHeaderList()) {
            paragraphs.addAll(header.getParagraphs());
            for (XWPFTable table : header.getTables()) {
                collectTableParagraphs(table, paragraphs);
            }
        }
        for (XWPFFooter footer : doc.getFooterList()) {
            paragraphs.addAll(footer.getParagraphs());
            for (XWPFTable table : footer.getTables()) {
                collectTableParagraphs(table, paragraphs);
            }
        }

        for (XWPFParagraph p : paragraphs) {
            for (XWPFRun r : p.getRuns()) {
                CTR ctr = r.getCTR();
                if (ctr != null) {
                    for (CTDrawing drawing : ctr.getDrawingList()) {
                        for (CTInline inline : drawing.getInlineList()) {
                            if (isPictureGraphic(inline.getGraphic()) && isCountedExtent(inline.getExtent())) {
                                return true;
                            }
                        }
                        for (CTAnchor anchor : drawing.getAnchorList()) {
                            if (isPictureGraphic(anchor.getGraphic()) && isCountedExtent(anchor.getExtent())) {
                                return true;
                            }
                        }
                    }
                }
                for (XWPFPicture pic : r.getEmbeddedPictures()) {
                    if (pic.getWidth() >= MIN_DRAWN_POINTS && pic.getDepth() >= MIN_DRAWN_POINTS) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void collectTableParagraphs(XWPFTable table, List<XWPFParagraph> paragraphs) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                paragraphs.addAll(cell.getParagraphs());
                for (XWPFTable nested : cell.getTables()) {
                    collectTableParagraphs(nested, paragraphs);
                }
            }
        }
    }

    private static boolean isPictureGraphic(CTGraphicalObject graphic) {
        if (graphic == null || graphic.getGraphicData() == null) return false;
        String uri = graphic.getGraphicData().getUri();
        if ("http://schemas.openxmlformats.org/drawingml/2006/picture".equals(uri)) {
            return true;
        }
        String xml = graphic.getGraphicData().toString();
        return xml.contains(":pic") || xml.contains("<pic ");
    }

    private static boolean isCountedExtent(CTPositiveSize2D extent) {
        if (extent == null) return false;
        double widthPt = (extent.getCx() * POINTS_PER_INCH) / EMU_PER_INCH;
        double heightPt = (extent.getCy() * POINTS_PER_INCH) / EMU_PER_INCH;
        return widthPt >= MIN_DRAWN_POINTS && heightPt >= MIN_DRAWN_POINTS;
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
     * Splits raw text into named sections using canonical alias table and line normalization.
     * Returns a map of canonical section name -> section body text.
     */
    public Map<String, String> extractSections(String rawText) {
        Map<String, String> sections = new LinkedHashMap<>();
        if (rawText == null || rawText.isBlank()) {
            return sections;
        }
        String[] lines = rawText.split("\n");
        String currentSection = "HEADER";
        StringBuilder body = new StringBuilder();

        for (String line : lines) {
            Optional<String> canonical = findCanonicalSection(line);
            if (canonical.isPresent()) {
                appendSectionBody(sections, currentSection, body.toString());
                currentSection = canonical.get();
                body = new StringBuilder();
            } else {
                body.append(line).append('\n');
            }
        }
        appendSectionBody(sections, currentSection, body.toString());
        return sections;
    }

    private static void appendSectionBody(Map<String, String> sections, String sectionName, String bodyText) {
        String trimmed = bodyText.trim();
        if (sections.containsKey(sectionName)) {
            String existing = sections.get(sectionName);
            if (existing.isBlank()) {
                sections.put(sectionName, trimmed);
            } else if (!trimmed.isBlank()) {
                sections.put(sectionName, existing + "\n\n" + trimmed);
            }
        } else {
            sections.put(sectionName, trimmed);
        }
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