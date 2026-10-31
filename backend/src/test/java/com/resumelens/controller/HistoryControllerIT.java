package com.resumelens.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumelens.TestData;
import com.resumelens.entity.JobDescription;
import com.resumelens.entity.MatchResult;
import com.resumelens.entity.Resume;
import com.resumelens.repository.JobDescriptionRepository;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.ResumeRepository;
import com.resumelens.repository.SectionScoreRepository;
import com.resumelens.repository.UserRepository;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GET /api/v1/history.
 *
 * Verifies:
 * - A valid token returns only that user's analyses, ordered by date descending.
 * - A request without a token returns exactly 401 with the standard ApiResponse JSON body.
 * - Two users cannot see each other's history.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SuppressWarnings("null")
class HistoryControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MatchResultRepository matchResultRepository;
    @Autowired
    ResumeRepository resumeRepository;
    @Autowired
    JobDescriptionRepository jobDescriptionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SectionScoreRepository sectionScoreRepository;

    private static final String JD_TEXT = "java spring boot docker developer role";

    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() throws Exception {
        tokenA = registerAndLogin(TestData.USER_EMAIL, TestData.USER_PASSWORD, TestData.USER_NAME);
        tokenB = registerAndLogin(TestData.USER2_EMAIL, TestData.USER2_PASSWORD, TestData.USER2_NAME);
    }

    // No token -> 401 with standard JSON body

    @Test
    void should_return401WithJsonBody_whenRequestHasNoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/v1/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // Authenticated user gets only their own history

    @Test
    void should_returnEmptyList_whenAuthenticatedUserHasNoAnalyses() throws Exception {
        String body = mockMvc.perform(get("/api/v1/history")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        JsonNode items = mapper.readTree(body).path("data");
        assertTrue(items.isArray(), "History data must be an array");
        assertEquals(0, items.size(),
                "User with no analyses must see an empty history list");
    }

    @Test
    void should_returnOnlyOwnAnalyses_whenAuthenticatedUserHasCompletedAnalysis() throws Exception {
        // Upload + analyse for user A
        UUID resumeIdA = uploadPdfAs(tokenA);
        runAnalysis(resumeIdA, JD_TEXT, "Software Engineer");

        // User A's history should have 1 item
        String bodyA = mockMvc.perform(get("/api/v1/history")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode itemsA = mapper.readTree(bodyA).path("data");
        assertEquals(1, itemsA.size(),
                "User A must see exactly 1 analysis in history");

        // User B's history must still be empty
        String bodyB = mockMvc.perform(get("/api/v1/history")
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode itemsB = mapper.readTree(bodyB).path("data");
        assertEquals(0, itemsB.size(),
                "User B must not see User A's analyses");
    }

    @Test
    void should_returnHistoryOrderedByDateDescending_whenMultipleAnalysesExist() throws Exception {
        // Run two analyses for user A
        UUID resumeId = uploadPdfAs(tokenA);
        runAnalysis(resumeId, JD_TEXT + " react", "Frontend Developer");
        runAnalysis(resumeId, JD_TEXT + " kubernetes", "DevOps Engineer");

        String body = mockMvc.perform(get("/api/v1/history")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode items = mapper.readTree(body).path("data");
        assertEquals(2, items.size(), "User A must see 2 analyses");

        String id0 = items.get(0).path("id").asText();
        String id1 = items.get(1).path("id").asText();
        assertNotNull(UUID.fromString(id0), "Item 0 must have a valid UUID id");
        assertNotNull(UUID.fromString(id1), "Item 1 must have a valid UUID id");
        assertNotEquals(id0, id1, "Both history items must have distinct UUIDs");

        String resId0 = items.get(0).path("resumeId").asText();
        assertEquals(resumeId.toString(), resId0, "resumeId in history must match the uploaded resume publicId");
    }

    @Test
    void should_returnAtMost50Results_whenUserHasMoreThan50Analyses() throws Exception {
        var user = userRepository.findByEmail(TestData.USER_EMAIL).orElseThrow();
        var resume = resumeRepository.save(new Resume(user, "test.pdf", "sample resume text", "Dev", 3));
        var jd = jobDescriptionRepository.save(new JobDescription("Dev", JD_TEXT, "skills"));
        for (int i = 0; i < 55; i++) {
            MatchResult mr = new MatchResult(resume, jd);
            mr.setOverallScore(75.0);
            mr.setStatusLabel("Strong Match");
            matchResultRepository.save(mr);
        }

        String body = mockMvc.perform(get("/api/v1/history")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode items = mapper.readTree(body).path("data");
        assertEquals(50, items.size(), "History must cap returned results to latest 50");
    }

    // DELETE /api/v1/history/{id}

    @Test
    void should_deleteHistoryItemAndReturn204_whenCallerIsOwner() throws Exception {
        UUID resumeId = uploadPdfAs(tokenA);
        runAnalysis(resumeId, JD_TEXT, "Software Engineer");

        var userA = userRepository.findByEmail(TestData.USER_EMAIL).orElseThrow();
        var historyItems = matchResultRepository.findByResumeUserIdOrderByAnalyzedAtDesc(userA.getId(), org.springframework.data.domain.Pageable.unpaged());
        assertEquals(1, historyItems.size());
        MatchResult mr = historyItems.get(0);
        UUID mrPublicId = mr.getPublicId();

        var scoresBefore = sectionScoreRepository.findByMatchResultId(mr.getId());
        assertFalse(scoresBefore.isEmpty(), "Section scores must exist before delete");

        mockMvc.perform(delete("/api/v1/history/" + mrPublicId)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        assertFalse(matchResultRepository.findByPublicId(mrPublicId).isPresent());
        assertTrue(sectionScoreRepository.findByMatchResultId(mr.getId()).isEmpty());
        assertFalse(resumeRepository.findByPublicId(resumeId).isPresent());
    }

    @Test
    void should_preserveResume_whenDeletingHistoryItemIfOtherMatchResultUsesIt() throws Exception {
        UUID resumeId = uploadPdfAs(tokenA);
        runAnalysis(resumeId, JD_TEXT + " v1", "Dev 1");
        runAnalysis(resumeId, JD_TEXT + " v2", "Dev 2");

        var userA = userRepository.findByEmail(TestData.USER_EMAIL).orElseThrow();
        var historyItems = matchResultRepository.findByResumeUserIdOrderByAnalyzedAtDesc(userA.getId(), org.springframework.data.domain.Pageable.unpaged());
        assertEquals(2, historyItems.size());

        MatchResult toDelete = historyItems.get(0);
        MatchResult toKeep = historyItems.get(1);

        mockMvc.perform(delete("/api/v1/history/" + toDelete.getPublicId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        assertFalse(matchResultRepository.findByPublicId(toDelete.getPublicId()).isPresent());
        assertTrue(matchResultRepository.findByPublicId(toKeep.getPublicId()).isPresent());
        assertTrue(resumeRepository.findByPublicId(resumeId).isPresent());
    }

    @Test
    void should_return404_whenDeletingAnotherUsersHistoryItem() throws Exception {
        UUID resumeIdA = uploadPdfAs(tokenA);
        runAnalysis(resumeIdA, JD_TEXT, "Software Engineer");

        var userA = userRepository.findByEmail(TestData.USER_EMAIL).orElseThrow();
        var historyItemsA = matchResultRepository.findByResumeUserIdOrderByAnalyzedAtDesc(userA.getId(), org.springframework.data.domain.Pageable.unpaged());
        UUID mrPublicIdA = historyItemsA.get(0).getPublicId();

        mockMvc.perform(delete("/api/v1/history/" + mrPublicIdA)
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        assertTrue(matchResultRepository.findByPublicId(mrPublicIdA).isPresent());
    }

    @Test
    void should_return401_whenDeletingHistoryItemUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/history/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return404_whenDeletingUnknownHistoryItem() throws Exception {
        mockMvc.perform(delete("/api/v1/history/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    // Helpers

    private String registerAndLogin(String email, String password, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.registerJson(email, password, name)))
                .andExpect(status().isCreated());

        String body = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = mapper.readTree(body).path("data").path("token").asText();
        assertFalse(token.isBlank(), "Login must return a non-blank token for " + email);
        return token;
    }

    private UUID uploadPdfAs(String token) throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                ResumeControllerIT.buildMinimalPdf());

        String body = mockMvc.perform(multipart("/api/v1/resumes/upload")
                .file(pdf)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String idStr = mapper.readTree(body).path("data").path("resumeId").asText();
        return UUID.fromString(idStr);
    }

    private void runAnalysis(UUID resumeId, String rawJd, String jobTitle) throws Exception {
        mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(resumeId, rawJd, jobTitle)))
                .andExpect(status().isOk());
    }
}

