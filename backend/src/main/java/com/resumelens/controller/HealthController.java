package com.resumelens.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint.
 * Performs a lightweight 'SELECT 1' query against the database.
 * This guarantees database connectivity and prevents Supabase free-tier
 * databases from pausing due to 7 days of inactivity.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        boolean dbHealthy = false;
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbHealthy = (result != null && result == 1);
        } catch (Exception e) {
            dbHealthy = false;
        }

        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "database", dbHealthy ? "CONNECTED" : "UNAVAILABLE"
        ));
    }
}