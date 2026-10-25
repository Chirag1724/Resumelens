package com.resumelens.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for POST /api/v1/resumes/upload.
 * PDFs are generated in-memory (no binary fixtures committed).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ResumeControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    // Successful upload

    @Test
    void should_return200WithResumeId_whenValidPdfUploaded() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", buildMinimalPdf());

        String body = mockMvc.perform(multipart("/api/v1/resumes/upload").file(pdf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse().getContentAsString();

        JsonNode data = mapper.readTree(body).path("data");
        String resumeIdStr = data.path("resumeId").asText();
        assertNotNull(resumeIdStr);
        UUID parsedUuid = assertDoesNotThrow(() -> UUID.fromString(resumeIdStr),
                "Uploaded resume must receive a valid UUID publicId");
        assertNotNull(parsedUuid);
    }

    // Unsupported file type

    @Test
    void should_return415_whenUploadedFileIsNotPdfOrDocx() throws Exception {
        MockMultipartFile txt = new MockMultipartFile(
                "file", "resume.txt", "text/plain", "John Doe resume".getBytes());

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(txt))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // Wrong magic bytes

    @Test
    void should_return415_whenPdfHasWrongMagicBytes() throws Exception {
        MockMultipartFile badPdf = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "Not a valid PDF content".getBytes());

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(badPdf))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void should_return415_whenDocxHasWrongMagicBytes() throws Exception {
        MockMultipartFile badDocx = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "Not a valid DOCX zip stream".getBytes());

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(badDocx))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // File over 10MB

    @Test
    void should_return413_whenFileExceeds10MB() throws Exception {
        byte[] oversizedBytes = new byte[10 * 1024 * 1024 + 1024]; // 10MB + 1KB
        MockMultipartFile hugeFile = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", oversizedBytes);

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(hugeFile))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // Corrupt DOCX

    @Test
    void should_return422_whenDocxIsCorrupt() throws Exception {
        // Starts with valid PK 0x03 0x04 magic bytes, but remainder is garbage
        byte[] corruptDocxBytes = new byte[] { 'P', 'K', 0x03, 0x04, 0x00, 0x00, 0x7f, 0x7f, 0x12, 0x34 };
        MockMultipartFile corruptDocx = new MockMultipartFile(
                "file", "corrupt.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                corruptDocxBytes);

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(corruptDocx))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // No extractable text

    @Test
    void should_return422_whenUploadHasNoExtractableText() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            doc.save(baos);
        }
        MockMultipartFile scannedPdf = new MockMultipartFile(
                "file", "scanned.pdf", "application/pdf", baos.toByteArray());

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(scannedPdf))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // Empty file

    @Test
    void should_return400_whenUploadedFileIsEmpty() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // Helper

    static byte[] buildMinimalPdf() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(50, 700);
                String[] lines = {
                        "EDUCATION",
                        "Bachelor of Computer Science 2020",
                        "EXPERIENCE",
                        "Software Engineer achieved 40% improvement using java and python",
                        "SKILLS",
                        "java python docker kubernetes spring"
                };
                for (String line : lines) {
                    cs.showText(line);
                    cs.newLineAtOffset(0, -14);
                }
                cs.endText();
            }
            doc.save(baos);
        }
        return baos.toByteArray();
    }
}

