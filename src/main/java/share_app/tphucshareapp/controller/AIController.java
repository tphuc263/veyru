package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.ai.CaptionSuggestionRequest;
import share_app.tphucshareapp.dto.request.ai.EngagementAnalysisRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.ai.CaptionSuggestionResponse;
import share_app.tphucshareapp.dto.response.ai.EngagementAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.PostTimingSuggestionResponse;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.service.ai.AIService;
import share_app.tphucshareapp.service.user.UserService;

@RestController
@RequestMapping("${api.prefix}/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final UserService userService;


    @PostMapping("/suggest-caption")
    public ResponseEntity<ApiResponse<CaptionSuggestionResponse>> suggestCaption(
            @RequestBody CaptionSuggestionRequest request) {
        CaptionSuggestionResponse response = aiService.suggestCaptions(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Caption suggestions generated"));
    }

    @PostMapping("/analyze-engagement")
    public ResponseEntity<ApiResponse<EngagementAnalysisResponse>> analyzeEngagement(
            @RequestBody(required = false) EngagementAnalysisRequest request) {
        User currentUser = userService.getCurrentUser();
        int postCount = request != null && request.getRecentPostCount() > 0
                ? request.getRecentPostCount() : 20;
        EngagementAnalysisResponse response = aiService.analyzeEngagement(currentUser.getId(), postCount);
        return ResponseEntity.ok(ApiResponse.success(response, "Engagement analysis completed"));
    }

    @GetMapping("/suggest-timing")
    public ResponseEntity<ApiResponse<PostTimingSuggestionResponse>> suggestPostTiming() {
        User currentUser = userService.getCurrentUser();
        PostTimingSuggestionResponse response = aiService.suggestPostTiming(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Timing suggestions generated"));
    }
}
