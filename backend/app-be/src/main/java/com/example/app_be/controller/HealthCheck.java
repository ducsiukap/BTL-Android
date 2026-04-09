package com.example.app_be.controller.dto;

import com.example.app_be.core.annotation.ApiV1;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@ApiV1
@RequestMapping("/health")
public class HealthCheck {

    @GetMapping
    ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
