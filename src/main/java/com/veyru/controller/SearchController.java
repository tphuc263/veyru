package com.veyru.controller;

import com.veyru.dto.response.PageResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.dto.response.search.UserSearchResponseSimple;
import com.veyru.service.photo.ExploreService;
import com.veyru.service.search.SearchService;
import com.veyru.service.user.UserService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/search")
public class SearchController {
  private static final Logger log = LoggerFactory.getLogger(SearchController.class);
  private final SearchService searchService;
  private final ExploreService exploreService;
  private final UserService userService;

  @GetMapping("/users")
  public ResponseEntity<PageResponse<UserSearchResponseSimple>> searchUsers(
      @RequestParam("q") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<UserSearchResponseSimple> users = searchService.searchUsers(query, page, size);
    return ResponseEntity.ok(PageResponse.from(users));
  }

  @GetMapping("/photos")
  public ResponseEntity<PageResponse<PhotoResponse>> searchPhotos(
      @RequestParam("q") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<PhotoResponse> photos = searchService.searchPhotos(query, page, size);
    return ResponseEntity.ok(PageResponse.from(photos));
  }

  @GetMapping("/photos/tags")
  public ResponseEntity<PageResponse<PhotoResponse>> searchPhotosByTags(
      @RequestParam("q") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<PhotoResponse> photos = searchService.searchPhotosByTags(query, page, size);
    return ResponseEntity.ok(PageResponse.from(photos));
  }

  @GetMapping("/suggestions")
  public ResponseEntity<List<String>> getSearchSuggestions(
      @RequestParam("q") String query, @RequestParam(defaultValue = "10") int limit) {
    List<String> suggestions = searchService.getSearchSuggestions(query, limit);
    return ResponseEntity.ok(suggestions);
  }

  // ── Explore (discover) endpoints ──
  @GetMapping("/explore")
  public ResponseEntity<PageResponse<PhotoResponse>> getExploreFeed(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    try {
      String userId = userService.getCurrentUser().getId();
      log.info("Fetching explore feed for user: {}", userId);
      Page<PhotoResponse> exploreFeed = exploreService.getExploreFeed(userId, page, size);
      return ResponseEntity.ok(PageResponse.from(exploreFeed));
    } catch (Exception e) {
      log.error("Error fetching explore feed: ", e);
      throw e;
    }
  }

  @GetMapping("/explore/popular")
  public ResponseEntity<PageResponse<PhotoResponse>> getPopularPhotos(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching popular photos, page: {}, size: {}", page, size);
    Page<PhotoResponse> popular = exploreService.getPopularPhotos(page, size);
    return ResponseEntity.ok(PageResponse.from(popular));
  }

  @GetMapping("/explore/tags/{tag}")
  public ResponseEntity<PageResponse<PhotoResponse>> getPhotosByTag(
      @PathVariable String tag,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching photos for tag: {}", tag);
    Page<PhotoResponse> photos = exploreService.getPhotosByTag(tag, page, size);
    return ResponseEntity.ok(PageResponse.from(photos));
  }

  public SearchController(
      final SearchService searchService,
      final ExploreService exploreService,
      final UserService userService) {
    this.searchService = searchService;
    this.exploreService = exploreService;
    this.userService = userService;
  }
}
