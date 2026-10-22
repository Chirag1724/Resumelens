package com.resumelens.service;

import com.resumelens.exception.UnsupportedFileTypeException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    // Helpers

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
