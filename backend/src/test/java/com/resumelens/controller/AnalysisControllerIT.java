package com.resumelens.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumelens.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the full analysis pipeline:
 * upload -> match -> get result -> fix-prompt -> report
 *
 * All 6 section scores are verified to be in valid ranges [0, 100].
 * Fix-prompt and report endpoints are exercised for a real MatchResult UUID.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SuppressWarnings("null")
class AnalysisControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    /** Shared across every test method via @BeforeEach. */
    private UUID resumeId;
    private UUID matchResultId;

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

        String resumeIdStr = mapper.readTree(uploadBody).path("data").path("resumeId").asText();
        resumeId = UUID.fromString(resumeIdStr);
        assertNotNull(resumeId, "Resume upload must return a valid UUID in @BeforeEach");

        // 2. Run analysis
        String matchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(resumeId, SAMPLE_JD, "Software Engineer")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String matchResultIdStr = mapper.readTree(matchBody).path("data").path("id").asText();
        matchResultId = UUID.fromString(matchResultIdStr);
        assertNotNull(matchResultId, "Analysis match must return a valid id UUID in @BeforeEach");
    }

    // GET /api/v1/analysis/{id} - all 6 sections in range

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

    // GET /api/v1/analysis/{id}/fix-prompt - 200

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

    // GET /api/v1/analysis/{id}/report - 200 with PDF bytes

    @Test
    void should_return200WithPdfBytes_forReportEndpoint_givenValidMatchResultId() throws Exception {
        byte[] pdf = mockMvc.perform(get("/api/v1/analysis/{id}/report", matchResultId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andReturn().getResponse().getContentAsByteArray();

        assertTrue(pdf.length > 0, "PDF report must not be empty");
        // PDF magic bytes: %PDF-
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
        assertEquals('-', (char) pdf[4]);
    }

    // Numeric IDs return 400 on all analysis routes

    @Test
    void should_return400_whenNumericIdProvidedOnAnalysisRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/analysis/12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(get("/api/v1/analysis/12345/report"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(get("/api/v1/analysis/12345/fix-prompt"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // Random UUID returns 404

    @Test
    void should_return404_whenRandomUuidProvidedOnAnalysisRoutes() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/analysis/{id}", randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.message").value("The requested resource was not found."));

        mockMvc.perform(get("/api/v1/analysis/{id}/report", randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.message").value("The requested resource was not found."));

        mockMvc.perform(get("/api/v1/analysis/{id}/fix-prompt", randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.message").value("The requested resource was not found."));
    }

    // Match with random resumeId UUID returns 404

    @Test
    void should_return404_whenMatchWithRandomResumeId() throws Exception {
        mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(UUID.randomUUID(), SAMPLE_JD, "Software Engineer")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.message").value("The requested resource was not found."));
    }

    // Full guest flow works with UUIDs

    @Test
    void should_completeFullGuestFlowWithUuids() throws Exception {
        // Step 1: Upload resume as guest (no auth header)
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "guest_resume.pdf", "application/pdf",
                ResumeControllerIT.buildMinimalPdf());

        String uploadBody = mockMvc.perform(multipart("/api/v1/resumes/upload").file(pdf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        String uploadedResumeId = mapper.readTree(uploadBody).path("data").path("resumeId").asText();
        UUID guestResumeUuid = assertDoesNotThrow(() -> UUID.fromString(uploadedResumeId));

        // Step 2: Match as guest
        String matchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(guestResumeUuid, SAMPLE_JD, "Guest Engineer")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        String matchIdStr = mapper.readTree(matchBody).path("data").path("id").asText();
        UUID guestMatchUuid = assertDoesNotThrow(() -> UUID.fromString(matchIdStr));

        // Step 3: Get analysis result as guest
        mockMvc.perform(get("/api/v1/analysis/{id}", guestMatchUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(guestMatchUuid.toString()));

        // Step 4: Get PDF report as guest
        mockMvc.perform(get("/api/v1/analysis/{id}/report", guestMatchUuid))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));

        // Step 5: Get fix prompt as guest
        mockMvc.perform(get("/api/v1/analysis/{id}/fix-prompt", guestMatchUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 404 body contains no class names

    @Test
    void should_containNoClassNames_in404ResponseBody() throws Exception {
        String responseBody = mockMvc.perform(get("/api/v1/analysis/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        assertFalse(responseBody.contains("MatchResult"), "404 response must not contain MatchResult class name");
        assertFalse(responseBody.contains("Resume"), "404 response must not contain Resume class name");
        assertFalse(responseBody.contains("JobDescription"), "404 response must not contain JobDescription class name");
        assertFalse(responseBody.contains("User"), "404 response must not contain User class name");
        assertFalse(responseBody.contains("com.resumelens"), "404 response must not contain package name");
        assertEquals("{\"success\":false,\"message\":\"The requested resource was not found.\",\"data\":null}", responseBody);
    }

    // Suggestions match between POST and GET

    @Test
    void should_returnSameSuggestionsInGetAsInPostMatch() throws Exception {
        String matchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(resumeId, SAMPLE_JD, "Software Engineer")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode postSuggestions = mapper.readTree(matchBody).path("data").path("suggestions");
        String idStr = mapper.readTree(matchBody).path("data").path("id").asText();
        UUID id = UUID.fromString(idStr);

        String getBody = mockMvc.perform(get("/api/v1/analysis/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode getSuggestions = mapper.readTree(getBody).path("data").path("suggestions");

        assertFalse(postSuggestions.isEmpty(), "POST suggestions must not be empty");
        assertEquals(postSuggestions, getSuggestions, "GET suggestions must equal POST suggestions for the same result");
    }

    // Risks match between POST and GET

    @Test
    void should_returnSameRisksInGetAsInPostMatch() throws Exception {
        String matchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(resumeId, SAMPLE_JD, "Software Engineer")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode postRisks = mapper.readTree(matchBody).path("data").path("risks");
        String idStr = mapper.readTree(matchBody).path("data").path("id").asText();
        UUID id = UUID.fromString(idStr);

        String getBody = mockMvc.perform(get("/api/v1/analysis/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode getRisks = mapper.readTree(getBody).path("data").path("risks");

        assertNotNull(postRisks, "POST risks must not be null");
        assertFalse(postRisks.isEmpty(), "POST risks must not be empty for sample resume");
        assertEquals(postRisks, getRisks, "GET risks must equal POST risks for the same result");
    }

    @Test
    void should_returnEqualRiskAndSuggestionFields_inPostAndGet() throws Exception {
        String matchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(resumeId, SAMPLE_JD, "Software Engineer")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode postRisks = mapper.readTree(matchBody).path("data").path("risks");
        JsonNode postSuggestions = mapper.readTree(matchBody).path("data").path("suggestions");
        String idStr = mapper.readTree(matchBody).path("data").path("id").asText();
        UUID id = UUID.fromString(idStr);

        String getBody = mockMvc.perform(get("/api/v1/analysis/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode getRisks = mapper.readTree(getBody).path("data").path("risks");
        JsonNode getSuggestions = mapper.readTree(getBody).path("data").path("suggestions");

        assertFalse(postRisks.isEmpty());
        assertEquals(postRisks.size(), getRisks.size());
        for (int i = 0; i < postRisks.size(); i++) {
            JsonNode pRisk = postRisks.get(i);
            JsonNode gRisk = getRisks.get(i);
            assertTrue(pRisk.hasNonNull("code"));
            assertTrue(pRisk.hasNonNull("points"));
            assertTrue(pRisk.hasNonNull("section"));
            assertEquals(pRisk.path("code").asText(), gRisk.path("code").asText());
            assertEquals(pRisk.path("points").asDouble(), gRisk.path("points").asDouble(), 0.001);
            assertEquals(pRisk.path("section").asText(), gRisk.path("section").asText());
        }

        assertFalse(postSuggestions.isEmpty());
        assertEquals(postSuggestions.size(), getSuggestions.size());
        for (int i = 0; i < postSuggestions.size(); i++) {
            JsonNode pSug = postSuggestions.get(i);
            JsonNode gSug = getSuggestions.get(i);
            assertTrue(pSug.hasNonNull("code"));
            assertEquals(pSug.path("code").asText(), gSug.path("code").asText());
        }
    }

    // Metadata fields match between POST and GET

    @Test
    void should_returnSameMetadataFieldsInGetAsInPostMatch() throws Exception {
        // Upload with targetRole and experienceYears
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "candidate-resume.pdf", "application/pdf",
                ResumeControllerIT.buildMinimalPdf());

        String uploadBody = mockMvc.perform(multipart("/api/v1/resumes/upload")
                .file(pdf)
                .param("targetRole", "Senior Java Engineer")
                .param("experienceYears", "7"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String customResumeIdStr = mapper.readTree(uploadBody).path("data").path("resumeId").asText();
        UUID customResumeId = UUID.fromString(customResumeIdStr);

        // POST /api/v1/analysis/match
        String matchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(customResumeId, SAMPLE_JD, "Senior Java Engineer")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode postData = mapper.readTree(matchBody).path("data");
        String idStr = postData.path("id").asText();
        UUID id = UUID.fromString(idStr);

        // GET /api/v1/analysis/{id}
        String getBody = mockMvc.perform(get("/api/v1/analysis/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode getData = mapper.readTree(getBody).path("data");

        // Assert 4 fields match between POST and GET and are populated
        assertEquals(postData.path("fileName").asText(), getData.path("fileName").asText());
        assertEquals("candidate-resume.pdf", postData.path("fileName").asText());

        assertEquals(postData.path("analyzedAt").asText(), getData.path("analyzedAt").asText());
        assertFalse(postData.path("analyzedAt").asText().isBlank());

        assertEquals(postData.path("targetRole").asText(), getData.path("targetRole").asText());
        assertEquals("Senior Java Engineer", postData.path("targetRole").asText());

        assertEquals(postData.path("experienceYears").asInt(), getData.path("experienceYears").asInt());
        assertEquals(7, postData.path("experienceYears").asInt());

        // Also verify that when absent, targetRole and experienceYears serialize as null
        String defaultGetBody = mockMvc.perform(get("/api/v1/analysis/{id}", matchResultId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode defaultData = mapper.readTree(defaultGetBody).path("data");
        assertTrue(defaultData.path("targetRole").isNull(), "targetRole must serialize as null when absent");
        assertTrue(defaultData.path("experienceYears").isNull(), "experienceYears must serialize as null when absent");
    }

    // CORS configuration: default origin allowed, different origin rejected

    @Test
    void should_allowDefaultOrigin_andRejectDifferentOrigin() throws Exception {
        // Allowed origin: preflight
        mockMvc.perform(options("/api/v1/analysis/match")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));

        // Allowed origin: actual request
        mockMvc.perform(get("/api/v1/analysis/{id}", matchResultId)
                .header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));

        // Disallowed origin: preflight rejected with 403
        mockMvc.perform(options("/api/v1/analysis/match")
                .header(HttpHeaders.ORIGIN, "http://untrusted-domain.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

        // Disallowed origin: actual request rejected with 403
        mockMvc.perform(get("/api/v1/analysis/{id}", matchResultId)
                .header(HttpHeaders.ORIGIN, "http://untrusted-domain.com"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    // Test (g): POST and GET return the same uncappedScore and scoreCapped

    @Test
    void should_returnSameUncappedScoreAndScoreCappedInGetAsInPostMatch() throws Exception {
        String matchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(resumeId, SAMPLE_JD, "Software Engineer")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode postData = mapper.readTree(matchBody).path("data");
        String idStr = postData.path("id").asText();
        UUID id = UUID.fromString(idStr);

        String getBody = mockMvc.perform(get("/api/v1/analysis/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode getData = mapper.readTree(getBody).path("data");

        assertFalse(postData.path("uncappedScore").isMissingNode());
        assertFalse(postData.path("scoreCapped").isMissingNode());
        assertFalse(getData.path("uncappedScore").isMissingNode());
        assertFalse(getData.path("scoreCapped").isMissingNode());

        assertEquals(postData.path("uncappedScore").asDouble(), getData.path("uncappedScore").asDouble(), 1e-9,
                "GET uncappedScore must equal POST uncappedScore");
        assertEquals(postData.path("scoreCapped").asBoolean(), getData.path("scoreCapped").asBoolean(),
                "GET scoreCapped must equal POST scoreCapped");

        // Test with strong resume against mismatched JD where score is capped at 54.0
        MockMultipartFile strongFile = new MockMultipartFile(
                "file", "strong.pdf", "application/pdf", buildStrongResumePdf());
        String strongUploadBody = mockMvc.perform(multipart("/api/v1/resumes/upload").file(strongFile))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID strongResumeId = UUID.fromString(mapper.readTree(strongUploadBody).path("data").path("resumeId").asText());

        String mlJd = "Machine Learning Engineer with deep learning, PyTorch, TensorFlow, computer vision, NLP, linear algebra, statistics, and MLOps.";
        String cappedMatchBody = mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(strongResumeId, mlJd, "ML Engineer")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode cappedPostData = mapper.readTree(cappedMatchBody).path("data");
        String cappedIdStr = cappedPostData.path("id").asText();
        UUID cappedId = UUID.fromString(cappedIdStr);

        String cappedGetBody = mockMvc.perform(get("/api/v1/analysis/{id}", cappedId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode cappedGetData = mapper.readTree(cappedGetBody).path("data");

        assertTrue(cappedPostData.path("scoreCapped").asBoolean(), "POST scoreCapped must be true for mismatched JD");
        assertTrue(cappedGetData.path("scoreCapped").asBoolean(), "GET scoreCapped must be true for mismatched JD");
        assertEquals(cappedPostData.path("uncappedScore").asDouble(), cappedGetData.path("uncappedScore").asDouble(), 1e-9,
                "GET uncappedScore must equal POST uncappedScore for capped result");
        assertEquals(cappedPostData.path("overallScore").asDouble(), cappedGetData.path("overallScore").asDouble(), 1e-9,
                "GET overallScore must equal POST overallScore for capped result");
        assertEquals(54.0, cappedPostData.path("overallScore").asDouble(), 0.01,
                "Capped overallScore must equal 54.0 literal");
    }

    static byte[] buildStrongResumePdf() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);
            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(50, 700);
                String[] lines = {
                        "Jane Doe",
                        "jane.doe@example.com 555-123-4567",
                        "EDUCATION",
                        "Bachelor of Computer Science 2020",
                        "EXPERIENCE",
                        "- Spearheaded development achieving 50% performance improvement",
                        "- Deployed cloud infrastructure saving $100k",
                        "PROJECTS",
                        "E-Commerce Platform",
                        "Java React deployed to 100k users achieving high throughput",
                        "SKILLS",
                        "Java React Spring Boot MySQL"
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


