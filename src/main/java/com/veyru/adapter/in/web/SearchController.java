package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.adapter.in.dto.response.search.UserSearchResponseSimple;
import com.veyru.application.discovery.ExploreService;
import com.veyru.application.discovery.SearchService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/search")
public class SearchController {
  private static final Logger log = LoggerFactory.getLogger(SearchController.class);
  private final SearchService searchService;
  private final ExploreService exploreService;

  @GetMapping("/users")
  public ResponseEntity<PageResponse<UserSearchResponseSimple>> searchUsers(
      @RequestParam("q") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var users = searchService.searchUsers(query, page, size);
    return ResponseEntity.ok(PageResponse.from(users, UserSearchResponseSimple::from));
  }

  @GetMapping("/photos")
  public ResponseEntity<PageResponse<PhotoResponse>> searchPhotos(
      @RequestParam("q") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var photos = searchService.searchPhotos(query, page, size);
    return ResponseEntity.ok(PageResponse.from(photos, PhotoResponse::from));
  }

  @GetMapping("/photos/tags")
  public ResponseEntity<PageResponse<PhotoResponse>> searchPhotosByTags(
      @RequestParam("q") String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var photos = searchService.searchPhotosByTags(query, page, size);
    return ResponseEntity.ok(PageResponse.from(photos, PhotoResponse::from));
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
    var exploreFeed = exploreService.getExploreFeed(null, page, size);
    return ResponseEntity.ok(PageResponse.from(exploreFeed, PhotoResponse::from));
  }

  @GetMapping("/explore/popular")
  public ResponseEntity<PageResponse<PhotoResponse>> getPopularPhotos(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching popular photos, page: {}, size: {}", page, size);
    var popular = exploreService.getPopularPhotos(page, size);
    return ResponseEntity.ok(PageResponse.from(popular, PhotoResponse::from));
  }

  @GetMapping("/explore/tags/{tag}")
  public ResponseEntity<PageResponse<PhotoResponse>> getPhotosByTag(
      @PathVariable String tag,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching photos for tag: {}", tag);
    var photos = exploreService.getPhotosByTag(tag, page, size);
    return ResponseEntity.ok(PageResponse.from(photos, PhotoResponse::from));
  }

  public SearchController(final SearchService searchService, final ExploreService exploreService) {
    this.searchService = searchService;
    this.exploreService = exploreService;
  }
}
