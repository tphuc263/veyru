package com.veyru.adapter.in.controller;

import com.veyru.domain.service.TagService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/tags")
public class TagController {
  private static final Logger log = LoggerFactory.getLogger(TagController.class);
  private final TagService tagService;

  /** Get trending hashtags Returns the most popular hashtags from the last 7 days */
  @GetMapping("/trending")
  public ResponseEntity<List<String>> getTrendingHashtags(
      @RequestParam(defaultValue = "10") int limit) {
    log.info("GET /api/v1/tags/trending?limit={}", limit);
    List<String> hashtags = tagService.getTrendingHashtags(limit);
    // Add # prefix for frontend display
    List<String> formattedHashtags = hashtags.stream().map(tag -> "#" + tag).toList();
    return ResponseEntity.ok(formattedHashtags);
  }

  /** Get popular hashtags (alias for trending) */
  @GetMapping("/popular")
  public ResponseEntity<List<String>> getPopularHashtags(
      @RequestParam(defaultValue = "10") int limit) {
    log.info("GET /api/v1/tags/popular?limit={}", limit);
    List<String> hashtags = tagService.getPopularHashtags(limit);
    // Add # prefix for frontend display
    List<String> formattedHashtags = hashtags.stream().map(tag -> "#" + tag).toList();
    return ResponseEntity.ok(formattedHashtags);
  }

  public TagController(final TagService tagService) {
    this.tagService = tagService;
  }
}
