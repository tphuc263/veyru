package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.adapter.in.dto.response.recommendation.RecommendedUserResponse;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.model.User;
import com.veyru.domain.service.ai.RecommendationService;
import com.veyru.domain.service.user.UserService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/recommendations")
public class RecommendationController {
  private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);
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
    } catch (ApiException e) {
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

  public RecommendationController(
      final RecommendationService recommendationService, final UserService userService) {
    this.recommendationService = recommendationService;
    this.userService = userService;
  }
}
