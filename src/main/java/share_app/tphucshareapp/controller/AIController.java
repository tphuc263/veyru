package share_app.tphucshareapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import share_app.tphucshareapp.dto.request.ai.EngagementAnalysisRequest;
import share_app.tphucshareapp.dto.request.ai.ImageAnalysisRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.ai.EngagementAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.ImageAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.PostTimingSuggestionResponse;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.service.ai.AIService;
import share_app.tphucshareapp.service.user.UserService;

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
