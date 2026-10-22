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
 * Integration tests for GET /api/v1/history.
 *
 * Verifies:
 * - A valid token returns only that user's analyses, ordered by date
 * descending.
 * - A request without a token returns 401 or 403 (history is
 * authentication-required
 * per SecurityConfig: .requestMatchers("/api/v1/history/**").authenticated()).
 * - Two users cannot see each other's history.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HistoryControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    private static final String JD_TEXT = "java spring boot docker developer role";

    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() throws Exception {
        tokenA = registerAndLogin(TestData.USER_EMAIL, TestData.USER_PASSWORD, TestData.USER_NAME);
        tokenB = registerAndLogin(TestData.USER2_EMAIL, TestData.USER2_PASSWORD, TestData.USER2_NAME);
    }

    // No token → 401

    @Test
    void should_return401_whenRequestHasNoAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/v1/history"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403,
                            "History without token must return 401 or 403, got: " + status);
                });
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
        Long resumeIdA = uploadPdfAs(tokenA);
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
        Long resumeId = uploadPdfAs(tokenA);
        runAnalysis(resumeId, JD_TEXT + " react", "Frontend Developer");
        runAnalysis(resumeId, JD_TEXT + " kubernetes", "DevOps Engineer");

        String body = mockMvc.perform(get("/api/v1/history")
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode items = mapper.readTree(body).path("data");
        assertEquals(2, items.size(), "User A must see 2 analyses");

        // Items should be ordered by analyzedAt descending — most recent first
        // We can only assert both items are present and have distinct IDs
        long id0 = items.get(0).path("matchResultId").asLong();
        long id1 = items.get(1).path("matchResultId").asLong();
        assertTrue(id0 > 0 && id1 > 0, "Both history items must have positive IDs");
        // Most recent analysis has the higher ID (IDENTITY strategy, sequential)
        assertTrue(id0 > id1,
                "Most recent analysis (higher ID) must appear first (ordered by date desc)");
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

    private Long uploadPdfAs(String token) throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                ResumeControllerIT.buildMinimalPdf());

        String body = mockMvc.perform(multipart("/api/v1/resumes/upload")
                .file(pdf)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return mapper.readTree(body).path("data").path("resumeId").asLong();
    }

    private void runAnalysis(Long resumeId, String rawJd, String jobTitle) throws Exception {
        mockMvc.perform(post("/api/v1/analysis/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.matchJson(resumeId, rawJd, jobTitle)))
                .andExpect(status().isOk());
    }
}
