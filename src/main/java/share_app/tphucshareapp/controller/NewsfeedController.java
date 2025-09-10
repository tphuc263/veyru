package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.service.photo.NewsfeedService;
import share_app.tphucshareapp.service.user.UserService;

@RestController
@RequestMapping("${api.prefix}/newsfeed")
@RequiredArgsConstructor
public class NewsfeedController {
    private final NewsfeedService newsfeedService;
    private final UserService userService;

    /**
     * Get user's personalized newsfeed
     * Uses smart caching strategy
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getNewsfeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Get current user from security context
        String userId = userService.getCurrentUser().getId();
        Page<PhotoResponse> newsfeed = newsfeedService.getSmartNewsfeed(userId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success(newsfeed, "Newsfeed retrieved successfully")
        );
    }

    /**
     * Force refresh newsfeed cache
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshNewsfeed() {
        String userId = userService.getCurrentUser().getId();
        newsfeedService.generateNewsfeedCache(userId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Newsfeed cache refreshed successfully")
        );
    }

    /**
     * Get real-time newsfeed (bypass cache)
     */
    @GetMapping("/realtime")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getRealtimeNewsfeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userId = userService.getCurrentUser().getId();
        Page<PhotoResponse> newsfeed = newsfeedService.getNewsfeed(userId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success(newsfeed, "Real-time newsfeed retrieved successfully")
        );
    }
}
