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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for POST /api/v1/resumes/upload.
 * PDFs are generated in-memory — no binary fixtures committed.
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
        assertTrue(data.path("resumeId").asLong() > 0,
                "Uploaded resume must receive a positive database-assigned ID");
    }

    // Unsupported file type

    @Test
    void should_return415_whenUploadedFileIsNotPdfOrDocx() throws Exception {
        MockMultipartFile txt = new MockMultipartFile(
                "file", "resume.txt", "text/plain", "John Doe resume".getBytes());

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(txt))
                .andExpect(status().isUnsupportedMediaType());
    }

    // Empty file

    @Test
    void should_return400_whenUploadedFileIsEmpty() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/v1/resumes/upload").file(empty))
                .andExpect(status().isBadRequest());
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
