package com.resumelens.service;

import com.resumelens.exception.UnsupportedFileTypeException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResumeParserService.
 * PDFs and DOCX files are generated in-memory (no committed binary fixtures).
 * PDFBox 3.x is used to create test PDFs; Apache POI for DOCX.
 */
class ResumeParserServiceTest {

    private static final String SAMPLE_TEXT = "EDUCATION\n" +
            "Bachelor of Computer Science University 2020\n" +
            "EXPERIENCE\n" +
            "Software Engineer at TechCorp 2020 to 2023\n" +
            "SKILLS\n" +
            "Java Python Docker Kubernetes\n" +
            "PROJECTS\n" +
            "Open Source Contribution Github\n" +
            "CERTIFICATIONS\n" +
            "AWS Certified Developer 2022";

    private final ResumeParserService service = new ResumeParserService();

    // PDF extraction

    @Test
    void should_extractTextFromPdf_whenFileContainsTextLayer() throws IOException {
        byte[] pdfBytes = buildSamplePdf(SAMPLE_TEXT);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes);

        String extracted = service.extractText(file);

        assertNotNull(extracted, "Extracted text must not be null");
        assertFalse(extracted.isBlank(), "Extracted text must not be blank");
        assertTrue(extracted.toUpperCase().contains("EDUCATION"),
                "Extracted PDF text must contain 'EDUCATION'");
        assertTrue(extracted.toUpperCase().contains("EXPERIENCE"),
                "Extracted PDF text must contain 'EXPERIENCE'");
    }

    // DOCX extraction

    @Test
    void should_extractTextFromDocx_whenFileContainsParagraphs() throws IOException {
        byte[] docxBytes = buildSampleDocx(SAMPLE_TEXT);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes);

        String extracted = service.extractText(file);

        assertNotNull(extracted, "Extracted text must not be null");
        assertFalse(extracted.isBlank(), "Extracted text must not be blank");
        assertTrue(extracted.toUpperCase().contains("SKILLS"),
                "Extracted DOCX text must contain 'SKILLS'");
        assertTrue(extracted.toUpperCase().contains("PROJECTS"),
                "Extracted DOCX text must contain 'PROJECTS'");
    }

    // Unsupported file type

    @Test
    void should_throwUnsupportedFileTypeException_whenFileIsNotPdfOrDocx() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", "text/plain", "some content".getBytes());

        assertThrows(UnsupportedFileTypeException.class, () -> service.extractText(file),
                "A .txt file must throw UnsupportedFileTypeException");
    }

    @Test
    void should_throwUnsupportedFileTypeException_forDocFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.doc", "application/msword", "content".getBytes());

        assertThrows(UnsupportedFileTypeException.class, () -> service.extractText(file),
                "A legacy .doc file must throw UnsupportedFileTypeException");
    }

    // Section extraction

    @Test
    void should_identifySkillsSection_whenStandardHeaderPresent() {
        Map<String, String> sections = service.extractSections(SAMPLE_TEXT);
        assertTrue(sections.containsKey("SKILLS"),
                "extractSections must identify SKILLS header");
        assertFalse(sections.get("SKILLS").isBlank(),
                "SKILLS section body must not be blank");
    }

    @Test
    void should_identifyExperienceSection_whenStandardHeaderPresent() {
        Map<String, String> sections = service.extractSections(SAMPLE_TEXT);
        assertTrue(sections.containsKey("EXPERIENCE") || sections.containsKey("WORK EXPERIENCE"),
                "extractSections must identify EXPERIENCE header");
    }

    @Test
    void should_identifyEducationSection_whenStandardHeaderPresent() {
        Map<String, String> sections = service.extractSections(SAMPLE_TEXT);
        assertTrue(sections.containsKey("EDUCATION"),
                "extractSections must identify EDUCATION header");
    }

    @Test
    void should_identifyProjectsSection_whenStandardHeaderPresent() {
        Map<String, String> sections = service.extractSections(SAMPLE_TEXT);
        assertTrue(sections.containsKey("PROJECTS"),
                "extractSections must identify PROJECTS header");
    }

    @Test
    void should_identifyCertificationsSection_whenStandardHeaderPresent() {
        Map<String, String> sections = service.extractSections(SAMPLE_TEXT);
        assertTrue(sections.containsKey("CERTIFICATIONS"),
                "extractSections must identify CERTIFICATIONS header");
    }

    @Test
    void should_returnNonEmptyMap_forTextWithNoRecognisedHeaders() {
        Map<String, String> sections = service.extractSections("Random unstructured text about work and skills.");
        assertNotNull(sections, "extractSections must never return null");
        // All content goes into the HEADER bucket when no known section is found
        assertFalse(sections.isEmpty(), "extractSections must return at least one bucket");
    }

    @Test
    void should_mapEachAliasToRightCanonicalSection_acrossAllVariations() {
        record TestCase(String heading, String canonical) {}
        List<TestCase> cases = List.of(
            new TestCase("Experience", "EXPERIENCE"),
            new TestCase("Work Experience", "EXPERIENCE"),
            new TestCase("Professional Experience", "EXPERIENCE"),
            new TestCase("Internships", "EXPERIENCE"),
            new TestCase("Employment History", "EXPERIENCE"),
            new TestCase("Education", "EDUCATION"),
            new TestCase("Academic Background", "EDUCATION"),
            new TestCase("Qualifications", "EDUCATION"),
            new TestCase("Skills", "SKILLS"),
            new TestCase("Technical Skills", "SKILLS"),
            new TestCase("Key Skills", "SKILLS"),
            new TestCase("Core Competencies", "SKILLS"),
            new TestCase("Projects", "PROJECTS"),
            new TestCase("Academic Projects", "PROJECTS"),
            new TestCase("Personal Projects", "PROJECTS"),
            new TestCase("Certifications", "CERTIFICATIONS"),
            new TestCase("Courses", "CERTIFICATIONS"),
            new TestCase("Achievements", "CERTIFICATIONS"),
            new TestCase("Awards", "CERTIFICATIONS")
        );

        for (TestCase tc : cases) {
            String[] variations = {
                tc.heading(),
                tc.heading() + ":",
                tc.heading().toUpperCase(java.util.Locale.ROOT),
                tc.heading().toUpperCase(java.util.Locale.ROOT) + ":",
                tc.heading().toLowerCase(java.util.Locale.ROOT),
                tc.heading().toLowerCase(java.util.Locale.ROOT) + ":"
            };
            for (String var : variations) {
                var canonicalOpt = ResumeParserService.findCanonicalSection(var);
                assertTrue(canonicalOpt.isPresent(),
                    "Heading '" + var + "' must be recognized as a heading");
                assertEquals(tc.canonical(), canonicalOpt.get(),
                    "Heading '" + var + "' must map to canonical section " + tc.canonical());

                String testText = var + "\nSection body line\n";
                Map<String, String> res = service.extractSections(testText);
                assertTrue(res.containsKey(tc.canonical()),
                    "extractSections must recognize '" + var + "' under canonical key " + tc.canonical());
                assertEquals("Section body line", res.get(tc.canonical()),
                    "Section body for '" + var + "' must match body line");
            }
        }
    }

    @Test
    void should_notTreatNormalSentenceAsHeading_suchAsExperienceWithReactAndNode() {
        String sentence = "Experience with React and Node";
        assertFalse(ResumeParserService.findCanonicalSection(sentence).isPresent(),
            "Sentence '" + sentence + "' must never be recognized as a canonical section heading");

        String sentenceWithPeriod = "Experience with React and Node.";
        assertFalse(ResumeParserService.findCanonicalSection(sentenceWithPeriod).isPresent(),
            "Sentence ending with period must never be recognized as a heading");

        String resumeText = sentence + "\nDeveloped full-stack features.\n";
        Map<String, String> sections = service.extractSections(resumeText);
        assertFalse(sections.containsKey("EXPERIENCE"),
            "extractSections must not create an EXPERIENCE section for '" + sentence + "'");
        assertTrue(sections.containsKey("HEADER"),
            "Content must remain under HEADER");
    }

    // Formatting risk detection

    @Test
    void should_detectFormattingRisk_whenMoreThan20PercentOfLinesHaveMultipleWideGaps() {
        // Each line has two 4+ space gaps simulating multi-column merge artefacts
        StringBuilder resume = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            resume.append("Java Developer     Senior     TechCorp\n"); // 2 gaps > 4 spaces
        }
        // 10 suspicious / 10 total = 100% > 20%
        assertTrue(service.detectFormattingRisks(resume.toString()),
                "Text where 100% of lines have wide gaps should be flagged as multi-column risk");
    }

    @Test
    void should_notFlagFormattingRisk_forNormalSingleColumnResume() {
        assertFalse(service.detectFormattingRisks(SAMPLE_TEXT),
                "Normal single-column resume text must not trigger formatting risk");
    }

    // Graphics detection tests

    @Test
    void should_notFlagGraphics_whenPdfImage400x400DrawnAt14x14Points() throws IOException {
        byte[] pdfBytes = buildPdfWithDrawnImage(400, 400, 14, 14);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes);
        assertFalse(service.hasGraphics(file),
                "A 400x400 image drawn at 14x14 points must not be flagged");
    }

    @Test
    void should_flagGraphics_whenPdfImage400x400DrawnAt200x200Points() throws IOException {
        byte[] pdfBytes = buildPdfWithDrawnImage(400, 400, 200, 200);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes);
        assertTrue(service.hasGraphics(file),
                "A 400x400 image drawn at 200x200 points must be flagged");
    }

    @Test
    void should_notFlagGraphics_whenPdfHasOnlyVectorShapes() throws IOException {
        byte[] pdfBytes = buildPdfWithVectorShapes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes);
        assertFalse(service.hasGraphics(file),
                "A PDF page with only vector shapes must not be flagged");
    }

    @Test
    void should_notDetectGraphics_whenPdfHasNoImages() throws IOException {
        byte[] pdfBytes = buildSamplePdf(SAMPLE_TEXT);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes);

        assertFalse(service.hasGraphics(file), "PDF without images must not be flagged with graphics risk");
    }

    @Test
    void should_notFlagGraphics_whenDocxImage400x400DrawnAt14x14Points() throws Exception {
        byte[] docxBytes = buildDocxWithDrawnImage(400, 400, 14, 14);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes);
        assertFalse(service.hasGraphics(file),
                "A 400x400 DOCX image drawn at 14x14 points must not be flagged");
    }

    @Test
    void should_flagGraphics_whenDocxImage400x400DrawnAt200x200Points() throws Exception {
        byte[] docxBytes = buildDocxWithDrawnImage(400, 400, 200, 200);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes);
        assertTrue(service.hasGraphics(file),
                "A 400x400 DOCX image drawn at 200x200 points must be flagged");
    }

    @Test
    void should_notFlagGraphics_whenDocxHasOnlyVectorShapes() throws Exception {
        byte[] docxBytes = buildDocxWithVectorShapes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes);
        assertFalse(service.hasGraphics(file),
                "A DOCX page with only vector shapes must not be flagged");
    }

    @Test
    void should_notDetectGraphics_whenDocxHasNoPictures() throws IOException {
        byte[] docxBytes = buildSampleDocx(SAMPLE_TEXT);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes);

        assertFalse(service.hasGraphics(file), "DOCX without pictures must not be flagged with graphics risk");
    }

    // Helpers

    private static byte[] buildPdfWithDrawnImage(int pixelWidth, int pixelHeight, float drawnWidthPt, float drawnHeightPt) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            BufferedImage bim = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB);
            PDImageXObject img = LosslessFactory.createFromImage(doc, bim);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(50, 700);
                cs.showText("Some text");
                cs.endText();
                cs.drawImage(img, 100, 100, drawnWidthPt, drawnHeightPt);
            }
            doc.save(baos);
        }
        return baos.toByteArray();
    }

    private static byte[] buildPdfWithVectorShapes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.setLineWidth(2.0f);
                cs.moveTo(50, 50);
                cs.lineTo(200, 50);
                cs.lineTo(200, 200);
                cs.closePath();
                cs.stroke();
                cs.addRect(100, 100, 150, 150);
                cs.fill();
            }
            doc.save(baos);
        }
        return baos.toByteArray();
    }

    private static byte[] buildDocxWithDrawnImage(int pixelWidth, int pixelHeight, int drawnWidthPt, int drawnHeightPt) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            var p = doc.createParagraph();
            var r = p.createRun();
            r.setText("Some text");
            BufferedImage bim = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bim, "png", imgBaos);
            byte[] imageBytes = imgBaos.toByteArray();
            int widthEmu = (int) Math.round(drawnWidthPt * 914400.0 / 72.0);
            int heightEmu = (int) Math.round(drawnHeightPt * 914400.0 / 72.0);
            r.addPicture(new ByteArrayInputStream(imageBytes), Document.PICTURE_TYPE_PNG, "test.png", widthEmu, heightEmu);
            doc.write(baos);
        }
        return baos.toByteArray();
    }

    private static byte[] buildDocxWithVectorShapes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            var p = doc.createParagraph();
            var r = p.createRun();
            r.setText("Some text before shape");
            var drawing = r.getCTR().addNewDrawing();
            var inline = drawing.addNewInline();
            inline.addNewExtent().setCx(2540000L);
            inline.getExtent().setCy(2540000L);
            var docPr = inline.addNewDocPr();
            docPr.setId(1);
            docPr.setName("Shape 1");
            var graphic = inline.addNewGraphic();
            var graphicData = graphic.addNewGraphicData();
            graphicData.setUri("http://schemas.microsoft.com/office/word/2010/wordprocessingShape");
            doc.write(baos);
        }
        return baos.toByteArray();
    }

    private static byte[] buildSamplePdf(String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(50, 700);
                // PDFBox showText does not support newlines — write each line separately
                for (String line : text.split("\n")) {
                    String safe = line.replaceAll("[^\\x20-\\x7E]", " ");
                    cs.showText(safe);
                    cs.newLineAtOffset(0, -14);
                }
                cs.endText();
            }
            doc.save(baos);
        }
        return baos.toByteArray();
    }

    private static byte[] buildSampleDocx(String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            for (String line : text.split("\n")) {
                doc.createParagraph().createRun().setText(line);
            }
            doc.write(baos);
        }
        return baos.toByteArray();
    }
}
