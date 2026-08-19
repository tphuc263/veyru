package com.veyru.controller;

import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.dto.response.recommendation.RecommendedUserResponse;
import com.veyru.model.User;
import com.veyru.service.ai.RecommendationService;
import com.veyru.service.user.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

  private final RecommendationService recommendationService;
  private final UserService userService;

  /**
   * Get related/similar photos for a given photo (Explore page). e.g., GET
   * /api/v1/recommendations/photos/{photoId}/related?limit=12
   */
  @GetMapping("/photos/{photoId}/related")
  public ResponseEntity<List<PhotoResponse>> getRelatedPhotos(
      @PathVariable String photoId, @RequestParam(defaultValue = "12") int limit) {
    log.info("Getting related photos for photoId: {}, limit: {}", photoId, limit);

    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser();
    } catch (Exception e) {
      log.debug("No authenticated user for related photos");
    }

    List<PhotoResponse> relatedPhotos =
        recommendationService.getRelatedPhotos(photoId, limit, currentUser);
    return ResponseEntity.ok(relatedPhotos);
  }

  /**
   * Get suggested users to follow (Home page sidebar). e.g., GET
   * /api/v1/recommendations/users/suggested?limit=5
   */
  @GetMapping("/users/suggested")
  public ResponseEntity<List<RecommendedUserResponse>> getSuggestedUsers(
      @RequestParam(defaultValue = "5") int limit) {
    log.info("Getting suggested users, limit: {}", limit);

    String userId = userService.getCurrentUser().getId();
    List<RecommendedUserResponse> suggestions =
        recommendationService.getSuggestedUsers(userId, limit);
    return ResponseEntity.ok(suggestions);
  }

  /**
   * Admin endpoint: batch index all photos and users (for initial setup). POST
   * /api/v1/recommendations/admin/index-all
   */
  @PostMapping("/admin/index-all")
  public ResponseEntity<Map<String, Integer>> batchIndexAll() {
    log.info("Starting batch indexing of all photos and users");

    int photosIndexed = recommendationService.batchIndexAllPhotos();
    int usersIndexed = recommendationService.batchIndexAllUsers();

    Map<String, Integer> result =
        Map.of(
            "photosIndexed", photosIndexed,
            "usersIndexed", usersIndexed);

    return ResponseEntity.ok(result);
  }
}
