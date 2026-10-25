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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for POST /api/v1/auth/register and POST /api/v1/auth/login.
 * Uses H2 in-memory via the "test" profile. Each test method dirtiesContext to
 * guarantee a clean H2 schema and avoid unique-email conflicts.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerIT {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    // Register

    @Test
    void should_return201WithToken_whenUserRegistersSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.registerJson(TestData.USER_EMAIL, TestData.USER_PASSWORD, TestData.USER_NAME)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertFalse(data.path("token").asText().isBlank(),
                "Successful registration must return a non-blank JWT token");
    }

    @Test
    void should_return400_whenSameEmailRegisteredTwice() throws Exception {
        String body = TestData.registerJson(TestData.USER_EMAIL, TestData.USER_PASSWORD, TestData.USER_NAME);

        // First registration succeeds
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());

        // Second registration with the same email must fail with exactly 400 Bad Request
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // Login

    @Test
    void should_return200WithToken_whenUserLogsInWithCorrectCredentials() throws Exception {
        // Register first
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.registerJson(TestData.USER_EMAIL, TestData.USER_PASSWORD, TestData.USER_NAME)))
                .andExpect(status().isCreated());

        // Then login
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.loginJson(TestData.USER_EMAIL, TestData.USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode data = mapper.readTree(result.getResponse().getContentAsString()).path("data");
        assertFalse(data.path("token").asText().isBlank(),
                "Login must return a non-blank JWT token");
    }

    @Test
    void should_return401_whenUserLogsInWithWrongPassword() throws Exception {
        // Register first
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.registerJson(TestData.USER_EMAIL, TestData.USER_PASSWORD, TestData.USER_NAME)))
                .andExpect(status().isCreated());

        // Login with wrong password
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.loginJson(TestData.USER_EMAIL, "WrongPassword!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void should_return401_whenLoginWithNonExistentEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestData.loginJson("nobody@example.com", "password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
