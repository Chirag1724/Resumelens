package com.resumelens.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumelens.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SuppressWarnings("null")
class UserControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    private String registerAndGetToken(String email, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.registerJson(email, TestData.USER_PASSWORD, name)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("token").asText();
    }

    @Test
    void should_authenticateViaOAuth_google() throws Exception {
        Map<String, String> body = Map.of(
                "provider", "GOOGLE",
                "email", "developer@gmail.com",
                "name", "Google Developer",
                "providerId", "google_12345"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/oauth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("developer@gmail.com"))
                .andExpect(jsonPath("$.data.name").value("Google Developer"))
                .andReturn();

        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertFalse(data.path("token").asText().isBlank());
    }

    @Test
    void should_authenticateViaOAuth_github() throws Exception {
        Map<String, String> body = Map.of(
                "provider", "GITHUB",
                "email", "octocat@github.com",
                "name", "Octocat",
                "providerId", "github_998877"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/oauth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("octocat@github.com"))
                .andExpect(jsonPath("$.data.name").value("Octocat"))
                .andReturn();

        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertFalse(data.path("token").asText().isBlank());
    }

    @Test
    void should_getProfile_whenAuthenticated() throws Exception {
        String token = registerAndGetToken("profileuser@example.com", "Profile User");

        mockMvc.perform(get("/api/v1/user/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("profileuser@example.com"))
                .andExpect(jsonPath("$.data.name").value("Profile User"))
                .andExpect(jsonPath("$.data.totalScans").value(0));
    }

    @Test
    void should_return401_whenGettingProfileWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_deleteAccount_whenAuthenticated() throws Exception {
        String token = registerAndGetToken("deleteuser@example.com", "Delete User");

        // Verify profile exists
        mockMvc.perform(get("/api/v1/user/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Delete account
        mockMvc.perform(delete("/api/v1/user/account")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Account and all associated evaluation data deleted successfully."));

        // Subsequent profile request with old token should fail with 401 Unauthorized because user is gone
        mockMvc.perform(get("/api/v1/user/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
