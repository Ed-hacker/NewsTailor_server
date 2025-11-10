package com.hongik.projectTNP.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    /**
     * ALB 헬스 체크용 엔드포인트
     * GET /health
     * GET /
     */
    @GetMapping({"/health", "/"})
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is running");
        return ResponseEntity.ok(response);
    }
}
