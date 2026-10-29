package com.resumelens.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthControllerTest {

    @Test
    void should_returnUpAndConnected_whenDatabaseQuerySucceeds() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        HealthController controller = new HealthController(jdbcTemplate);
        ResponseEntity<Map<String, String>> response = controller.health();

        assertEquals(200, response.getStatusCode().value());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("CONNECTED", body.get("database"));
    }

    @Test
    void should_returnUpAndUnavailable_whenDatabaseQueryFails() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(new RuntimeException("DB Connection Timeout"));

        HealthController controller = new HealthController(jdbcTemplate);
        ResponseEntity<Map<String, String>> response = controller.health();

        assertEquals(200, response.getStatusCode().value());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("UNAVAILABLE", body.get("database"));
    }
}
