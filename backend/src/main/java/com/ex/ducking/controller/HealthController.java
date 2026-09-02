package com.ex.ducking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        // Log print hoga jaise hi UptimeRobot request bhejega
        System.out.println(">>> [UptimeRobot Ping] Health check received at: " + LocalDateTime.now());

        try {

            jdbcTemplate.execute("SELECT 1");
            return ResponseEntity.ok("Service is UP and Database is ACTIVE");
        } catch (Exception e) {
            System.err.println(">>> [Health Check Failed]: " + e.getMessage());
            return ResponseEntity.status(500).body("Database Connection Failed: " + e.getMessage());
        }
    }
}