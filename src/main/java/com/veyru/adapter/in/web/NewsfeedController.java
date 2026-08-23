package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.adapter.in.dto.response.post.UnifiedPostResponse;
import com.veyru.application.discovery.NewsfeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/feed")
public class NewsfeedController {
  private static final Logger log = LoggerFactory.getLogger(NewsfeedController.class);
  private final NewsfeedService newsfeedService;

  /** Get user's personalized newsfeed Uses smart caching strategy */
  @GetMapping
  public ResponseEntity<PageResponse<PhotoResponse>> getNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    log.info("Fetching newsfeed for page: {}, size: {}", page, size);
    var newsfeed = newsfeedService.getSmartNewsfeed(page, size);
    log.info("Successfully fetched {} items for newsfeed", newsfeed.items().size());
    return ResponseEntity.ok(PageResponse.from(newsfeed, PhotoResponse::from));
  }

  @GetMapping("/realtime")
  public ResponseEntity<PageResponse<PhotoResponse>> getRealtimeNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    var newsfeed = newsfeedService.getNewsfeed(page, size);
    return ResponseEntity.ok(PageResponse.from(newsfeed, PhotoResponse::from));
  }

  /** Get unified newsfeed (photos + shares) */
  @GetMapping("/unified")
  public ResponseEntity<PageResponse<UnifiedPostResponse>> getUnifiedNewsfeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching unified newsfeed for page: {}, size: {}", page, size);
    var newsfeed = newsfeedService.getUnifiedNewsfeed(page, size);
    log.info("Successfully fetched {} items for unified newsfeed", newsfeed.items().size());
    return ResponseEntity.ok(PageResponse.from(newsfeed, UnifiedPostResponse::from));
  }

  public NewsfeedController(final NewsfeedService newsfeedService) {
    this.newsfeedService = newsfeedService;
  }
}
