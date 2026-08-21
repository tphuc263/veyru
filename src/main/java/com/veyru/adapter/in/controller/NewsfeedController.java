package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.adapter.in.dto.response.post.UnifiedPostResponse;
import com.veyru.domain.service.photo.NewsfeedService;
import com.veyru.domain.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/newsfeed")
public class NewsfeedController {
  private static final Logger log = LoggerFactory.getLogger(NewsfeedController.class);
  private final NewsfeedService newsfeedService;
  private final UserService userService;

  /** Get user's personalized newsfeed Uses smart caching strategy */
  @GetMapping
  public ResponseEntity<PageResponse<PhotoResponse>> getNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    try {
      log.info("Fetching newsfeed for page: {}, size: {}", page, size);
      // Get current user from security context
      String userId = userService.getCurrentUser().getId();
      log.info("Fetching newsfeed for user: {}", userId);
      Page<PhotoResponse> newsfeed = newsfeedService.getSmartNewsfeed(userId, page, size);
      log.info("Successfully fetched {} items for newsfeed", newsfeed.getContent().size());
      return ResponseEntity.ok(PageResponse.from(newsfeed));
    } catch (Exception e) {
      log.error("Error fetching newsfeed: ", e);
      throw e;
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<Void> refreshNewsfeed() {
    String userId = userService.getCurrentUser().getId();
    newsfeedService.generateNewsfeedCache(userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/realtime")
  public ResponseEntity<PageResponse<PhotoResponse>> getRealtimeNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    String userId = userService.getCurrentUser().getId();
    Page<PhotoResponse> newsfeed = newsfeedService.getNewsfeed(userId, page, size);
    return ResponseEntity.ok(PageResponse.from(newsfeed));
  }

  /** Get unified newsfeed (photos + shares) */
  @GetMapping("/unified")
  public ResponseEntity<PageResponse<UnifiedPostResponse>> getUnifiedNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    try {
      log.info("Fetching unified newsfeed for page: {}, size: {}", page, size);
      String userId = userService.getCurrentUser().getId();
      Page<UnifiedPostResponse> newsfeed = newsfeedService.getUnifiedNewsfeed(userId, page, size);
      log.info("Successfully fetched {} items for unified newsfeed", newsfeed.getContent().size());
      return ResponseEntity.ok(PageResponse.from(newsfeed));
    } catch (Exception e) {
      log.error("Error fetching unified newsfeed: ", e);
      throw e;
    }
  }

  public NewsfeedController(final NewsfeedService newsfeedService, final UserService userService) {
    this.newsfeedService = newsfeedService;
    this.userService = userService;
  }
}
