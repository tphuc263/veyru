package com.veyru.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.veyru.dto.request.ai.EngagementAnalysisRequest;
import com.veyru.dto.request.ai.ImageAnalysisRequest;
import com.veyru.dto.response.ApiResponse;
import com.veyru.dto.response.ai.EngagementAnalysisResponse;
import com.veyru.dto.response.ai.ImageAnalysisResponse;
import com.veyru.dto.response.ai.PostTimingSuggestionResponse;
import com.veyru.model.User;
import com.veyru.service.ai.AIService;
import com.veyru.service.user.UserService;

@RestController
@RequestMapping("${api.prefix}/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;
    private final UserService userService;

    @PostMapping("/analyze-engagement")
    public ResponseEntity<ApiResponse<EngagementAnalysisResponse>> analyzeEngagement(
            @RequestBody(required = false) EngagementAnalysisRequest request) {
        User currentUser = userService.getCurrentUser();
        int postCount = request != null && request.getRecentPostCount() > 0
                ? request.getRecentPostCount()
                : 20;
        EngagementAnalysisResponse response = aiService.analyzeEngagement(currentUser.getId(), postCount);
        return ResponseEntity.ok(ApiResponse.success(response, "Engagement analysis completed"));
    }

    @GetMapping("/suggest-timing")
    public ResponseEntity<ApiResponse<PostTimingSuggestionResponse>> suggestPostTiming() {
        User currentUser = userService.getCurrentUser();
        PostTimingSuggestionResponse response = aiService.suggestPostTiming(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Timing suggestions generated"));
    }

    @PostMapping("/analyze-image")
    public ResponseEntity<ApiResponse<ImageAnalysisResponse>> analyzeImage(
            @RequestBody ImageAnalysisRequest request) {
        // Get current user for personalization
        try {
            User currentUser = userService.getCurrentUser();
            request.setUserId(currentUser.getId());
        } catch (Exception e) {
            log.debug("No authenticated user for image analysis");
        }

        ImageAnalysisResponse response = aiService.analyzeImage(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Image analysis completed"));
    }
}
