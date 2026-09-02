package com.ex.ducking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        try {
            // Aiven DB ko active rakhne ke liye simple SQL query
            jdbcTemplate.execute("SELECT 1");
            return ResponseEntity.ok("Service is UP and Database is ACTIVE");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Database Connection Failed: " + e.getMessage());
        }
    }
}