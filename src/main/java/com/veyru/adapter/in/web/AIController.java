package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.request.ai.EngagementAnalysisRequest;
import com.veyru.adapter.in.dto.request.ai.ImageAnalysisRequest;
import com.veyru.adapter.in.dto.response.ai.EngagementAnalysisResponse;
import com.veyru.adapter.in.dto.response.ai.ImageAnalysisResponse;
import com.veyru.adapter.in.dto.response.ai.PostTimingSuggestionResponse;
import com.veyru.application.intelligence.AIService;
import com.veyru.application.intelligence.ImageAnalysisCommand;
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

  @PostMapping("/engagement-analyses")
  public ResponseEntity<EngagementAnalysisResponse> analyzeEngagement(
      @Valid @RequestBody(required = false) EngagementAnalysisRequest request) {
    int postCount =
        request != null && request.recentPostCount() > 0 ? request.recentPostCount() : 20;
    return ResponseEntity.ok(
        EngagementAnalysisResponse.from(aiService.analyzeEngagement(postCount)));
  }

  @GetMapping("/post-timing-suggestions")
  public ResponseEntity<PostTimingSuggestionResponse> suggestPostTiming() {
    return ResponseEntity.ok(PostTimingSuggestionResponse.from(aiService.suggestPostTiming()));
  }

  @PostMapping("/image-analyses")
  public ResponseEntity<ImageAnalysisResponse> analyzeImage(
      @Valid @RequestBody ImageAnalysisRequest request) {
    return ResponseEntity.ok(
        ImageAnalysisResponse.from(
            aiService.analyzeImageForCurrentActor(
                new ImageAnalysisCommand(
                    request.imageBase64(), request.mimeType(), request.userId()))));
  }

  public AIController(final AIService aiService) {
    this.aiService = aiService;
  }
}
