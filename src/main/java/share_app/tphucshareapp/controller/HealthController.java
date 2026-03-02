package share_app.tphucshareapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import share_app.tphucshareapp.dto.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint cho Docker healthcheck và monitoring
 */
@RestController
@RequestMapping("${api.prefix}")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "ShareApp Backend");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(ApiResponse.success(health, "Application is healthy"));
    }
}
