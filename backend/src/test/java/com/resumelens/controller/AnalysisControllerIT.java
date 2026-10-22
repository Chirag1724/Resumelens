package com.resumelens.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumelens.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the full analysis pipeline:
 * upload → match → get result → fix-prompt → report
 *
 * All 6 section scores are verified to be in valid ranges [0, 100].
 * Fix-prompt and report endpoints are exercised for a real matchResultId.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AnalysisControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    /** Shared across every test method via @BeforeEach. */
    private Long resumeId;
    private Long matchResultId;

    private static final String SAMPLE_JD = "We need a java developer with spring boot docker kubernetes experience";

    @BeforeEach
    void uploadAndAnalyse() throws Exception {
        // 1. Upload a valid PDF
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                ResumeControllerIT.buildMinimalPdf());

        String uploadBody = mockMvc.perform(multipart("/api/v1/resumes/upload").file(pdf))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        resumeId = mapper.readTree(uploadBody).path("data").path("resumeId").asLong();
        assertTrue(resumeId > 0, "Resume upload must return a valid ID in @BeforeEach");

        // 2. Run analysis
        String matchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(resumeId, SAMPLE_JD, "Software Engineer")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        matchResultId = mapper.readTree(matchBody).path("data").path("matchResultId").asLong();
        assertTrue(matchResultId > 0, "Analysis match must return a valid matchResultId in @BeforeEach");
    }

    // GET /api/v1/analysis/{id} — all 6 sections in range

    @Test
    void should_return200_withAll6SectionScoresInValidRange() throws Exception {
        String body = mockMvc.perform(get("/api/v1/analysis/{id}", matchResultId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        JsonNode data = mapper.readTree(body).path("data");

        String[] sections = { "atsScore", "skillsScore", "impactScore",
                "projectScore", "writingScore", "professionalismScore" };
        for (String section : sections) {
            double score = data.path(section).asDouble(-1.0);
            assertTrue(score >= 0.0 && score <= 100.0,
                    section + " must be in [0, 100] but was " + score);
        }

        double overall = data.path("overallScore").asDouble(-1.0);
        assertTrue(overall >= 0.0 && overall <= 100.0,
                "overallScore must be in [0, 100] but was " + overall);

        assertFalse(data.path("statusLabel").asText("").isBlank(),
                "statusLabel must not be blank");
    }

    @Test
    void should_returnMatchedAndMissingKeywordLists_inAnalysisResult() throws Exception {
        String body = mockMvc.perform(get("/api/v1/analysis/{id}", matchResultId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode data = mapper.readTree(body).path("data");
        JsonNode sections = data.path("sectionScores");
        assertTrue(sections.isArray() && !sections.isEmpty(),
                "Analysis result must include sectionScores");

        boolean hasSkillsSection = false;
        for (JsonNode sec : sections) {
            if ("SKILLS_VALIDATION".equals(sec.path("sectionName").asText())) {
                assertTrue(sec.has("matchedKeywords"),
                        "Skills section score must include matchedKeywords field");
                assertTrue(sec.has("missingKeywords"),
                        "Skills section score must include missingKeywords field");
                hasSkillsSection = true;
            }
        }
        assertTrue(hasSkillsSection, "Must contain SKILLS_VALIDATION section");
    }

    // GET /api/v1/analysis/{id}/fix-prompt — 200

    @Test
    void should_return200_forFixPromptEndpoint_givenValidMatchResultId() throws Exception {
        String body = mockMvc.perform(
                get("/api/v1/analysis/{id}/fix-prompt", matchResultId)
                        .param("templateStyle", "Simple Single-Column"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        String prompt = mapper.readTree(body).path("data").asText();
        assertFalse(prompt.isBlank(), "Fix prompt must not be blank");
        assertTrue(prompt.contains("LaTeX") || prompt.contains("latex") || prompt.contains("resume"),
                "Fix prompt must contain LaTeX / resume content keywords");
    }

    // GET /api/v1/analysis/{id}/report — 200 with PDF bytes

    @Test
    void should_return200WithPdfBytes_forReportEndpoint_givenValidMatchResultId() throws Exception {
        byte[] pdf = mockMvc.perform(get("/api/v1/analysis/{id}/report", matchResultId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andReturn().getResponse().getContentAsByteArray();

        assertTrue(pdf.length > 0, "PDF report must not be empty");
        // PDF magic bytes: %PDF
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    // 404 for unknown IDs

    @Test
    void should_return4xx_whenMatchResultIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/analysis/{id}", 999_999L))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status >= 400 && status < 500,
                            "Non-existent matchResultId must return 4xx, got: " + status);
                });
    }
}
