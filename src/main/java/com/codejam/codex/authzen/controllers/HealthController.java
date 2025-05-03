package com.codejam.codex.authzen.controllers;

import com.codejam.codex.authzen.constants.ApiEndpoint;
import com.codejam.codex.authzen.responses.AuthzenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping(ApiEndpoint.HEALTH)
    public ResponseEntity<AuthzenResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "AuthZen API");
        health.put("version", "1.0.0");
        health.put("timestamp", Instant.now().toString());

        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        String uptime = String.format(
            "%02dh:%02dm:%02ds",
            uptimeMillis / 3_600_000,
            (uptimeMillis / 60_000) % 60,
            (uptimeMillis / 1_000) % 60
        );
        health.put("uptime", uptime);

        // Use the static helper to produce a typed, successful response
        AuthzenResponse<Map<String, Object>> response =
            AuthzenResponse.success(health, "Health check successful");
        return ResponseEntity.ok(response);
    }
}
