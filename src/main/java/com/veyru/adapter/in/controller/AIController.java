package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.request.ai.EngagementAnalysisRequest;
import com.veyru.adapter.in.dto.request.ai.ImageAnalysisRequest;
import com.veyru.adapter.in.dto.response.ai.EngagementAnalysisResponse;
import com.veyru.adapter.in.dto.response.ai.ImageAnalysisResponse;
import com.veyru.adapter.in.dto.response.ai.PostTimingSuggestionResponse;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.model.User;
import com.veyru.domain.service.ai.AIService;
import com.veyru.domain.service.user.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/ai")
public class AIController {
  private static final Logger log = LoggerFactory.getLogger(AIController.class);
  private final AIService aiService;
  private final UserService userService;

  @PostMapping("/engagement-analyses")
  public ResponseEntity<EngagementAnalysisResponse> analyzeEngagement(
      @Valid @RequestBody(required = false) EngagementAnalysisRequest request) {
    User currentUser = userService.getCurrentUser();
    int postCount =
        request != null && request.getRecentPostCount() > 0 ? request.getRecentPostCount() : 20;
    EngagementAnalysisResponse response =
        aiService.analyzeEngagement(currentUser.getId(), postCount);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/post-timing-suggestions")
  public ResponseEntity<PostTimingSuggestionResponse> suggestPostTiming() {
    User currentUser = userService.getCurrentUser();
    PostTimingSuggestionResponse response = aiService.suggestPostTiming(currentUser.getId());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/image-analyses")
  public ResponseEntity<ImageAnalysisResponse> analyzeImage(
      @Valid @RequestBody ImageAnalysisRequest request) {
    ImageAnalysisRequest personalizedRequest = request;
    try {
      User currentUser = userService.getCurrentUser();
      personalizedRequest =
          new ImageAnalysisRequest(request.imageBase64(), request.mimeType(), currentUser.getId());
    } catch (ApiException e) {
      log.debug("No authenticated user for image analysis");
    }
    ImageAnalysisResponse response = aiService.analyzeImage(personalizedRequest);
    return ResponseEntity.ok(response);
  }

  public AIController(final AIService aiService, final UserService userService) {
    this.aiService = aiService;
    this.userService = userService;
  }
}
