package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.recommendation.RecommendedUserResponse;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.service.ai.RecommendationService;
import share_app.tphucshareapp.service.user.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    /**
     * Get related/similar photos for a given photo (Explore page).
     * e.g., GET /api/v1/recommendations/photos/{photoId}/related?limit=12
     */
    @GetMapping("/photos/{photoId}/related")
    public ResponseEntity<ApiResponse<List<PhotoResponse>>> getRelatedPhotos(
            @PathVariable String photoId,
            @RequestParam(defaultValue = "12") int limit) {
        log.info("Getting related photos for photoId: {}, limit: {}", photoId, limit);

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            log.debug("No authenticated user for related photos");
        }

        List<PhotoResponse> relatedPhotos = recommendationService.getRelatedPhotos(photoId, limit, currentUser);
        return ResponseEntity.ok(
                ApiResponse.success(relatedPhotos, "Related photos retrieved successfully")
        );
    }

    /**
     * Get suggested users to follow (Home page sidebar).
     * e.g., GET /api/v1/recommendations/users/suggested?limit=5
     */
    @GetMapping("/users/suggested")
    public ResponseEntity<ApiResponse<List<RecommendedUserResponse>>> getSuggestedUsers(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Getting suggested users, limit: {}", limit);

        String userId = userService.getCurrentUser().getId();
        List<RecommendedUserResponse> suggestions = recommendationService.getSuggestedUsers(userId, limit);
        return ResponseEntity.ok(
                ApiResponse.success(suggestions, "Suggested users retrieved successfully")
        );
    }

    /**
     * Admin endpoint: batch index all photos and users (for initial setup).
     * POST /api/v1/recommendations/admin/index-all
     */
    @PostMapping("/admin/index-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> batchIndexAll() {
        log.info("Starting batch indexing of all photos and users");

        int photosIndexed = recommendationService.batchIndexAllPhotos();
        int usersIndexed = recommendationService.batchIndexAllUsers();

        Map<String, Integer> result = Map.of(
                "photosIndexed", photosIndexed,
                "usersIndexed", usersIndexed
        );

        return ResponseEntity.ok(
                ApiResponse.success(result, "Batch indexing completed")
        );
    }
}
