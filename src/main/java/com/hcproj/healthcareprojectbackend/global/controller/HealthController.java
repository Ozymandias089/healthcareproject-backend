package com.hcproj.healthcareprojectbackend.global.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String version;

    @GetMapping(path = "/health", produces = "application/json")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP"));
    }

    @GetMapping(path = "/version", produces = "application/json")
    public ApiResponse<Map<String, Object>> version() {
        return ApiResponse.ok(Map.of(
                "name", appName,
                "version", version,
                "buildTime", Instant.now().toString()
        ));
    }
}
