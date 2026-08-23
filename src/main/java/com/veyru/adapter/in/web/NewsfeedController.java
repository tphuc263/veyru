package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.application.common.PageResult;
import com.veyru.application.discovery.NewsfeedService;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.result.photo.PhotoResponse;
import com.veyru.application.result.post.UnifiedPostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/feed")
public class NewsfeedController {
  private static final Logger log = LoggerFactory.getLogger(NewsfeedController.class);
  private final NewsfeedService newsfeedService;
  private final UserProfileService userService;

  /** Get user's personalized newsfeed Uses smart caching strategy */
  @GetMapping
  public ResponseEntity<PageResponse<PhotoResponse>> getNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    log.info("Fetching newsfeed for page: {}, size: {}", page, size);
    String userId = userService.getCurrentUser().getId();
    log.info("Fetching newsfeed for user: {}", userId);
    PageResult<PhotoResponse> newsfeed = newsfeedService.getSmartNewsfeed(userId, page, size);
    log.info("Successfully fetched {} items for newsfeed", newsfeed.items().size());
    return ResponseEntity.ok(PageResponse.from(newsfeed));
  }

  @GetMapping("/realtime")
  public ResponseEntity<PageResponse<PhotoResponse>> getRealtimeNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    String userId = userService.getCurrentUser().getId();
    PageResult<PhotoResponse> newsfeed = newsfeedService.getNewsfeed(userId, page, size);
    return ResponseEntity.ok(PageResponse.from(newsfeed));
  }

  /** Get unified newsfeed (photos + shares) */
  @GetMapping("/unified")
  public ResponseEntity<PageResponse<UnifiedPostResponse>> getUnifiedNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching unified newsfeed for page: {}, size: {}", page, size);
    String userId = userService.getCurrentUser().getId();
    PageResult<UnifiedPostResponse> newsfeed =
        newsfeedService.getUnifiedNewsfeed(userId, page, size);
    log.info("Successfully fetched {} items for unified newsfeed", newsfeed.items().size());
    return ResponseEntity.ok(PageResponse.from(newsfeed));
  }

  public NewsfeedController(
      final NewsfeedService newsfeedService, final UserProfileService userService) {
    this.newsfeedService = newsfeedService;
    this.userService = userService;
  }
}
