package com.veyru.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health check endpoint cho Docker healthcheck và monitoring */
@RestController
@RequestMapping("${api.prefix}")
public class HealthController {

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(
        Map.of(
            "status", "UP",
            "application", "Veyru Backend",
            "timestamp", String.valueOf(System.currentTimeMillis())));
  }
}
