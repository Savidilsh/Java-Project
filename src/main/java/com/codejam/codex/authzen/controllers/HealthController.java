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
    public ResponseEntity<AuthzenResponse<Map<String,Object>>> checkHealth() {
        Map<String,Object> health = new HashMap<>();
        health.put("status",      "UP");
        health.put("application", "AuthZen API");
        health.put("version",     "1.0.0");
        health.put("timestamp",   Instant.now().toString());
        long up = ManagementFactory.getRuntimeMXBean().getUptime();
        health.put("uptime", String.format("%02dh:%02dm:%02ds",
                up/3600000, (up/60000)%60, (up/1000)%60));
        AuthzenResponse<Map<String,Object>> r = new AuthzenResponse<>(health, true, "Health check successful");
        return ResponseEntity.ok(r);
    }
}
