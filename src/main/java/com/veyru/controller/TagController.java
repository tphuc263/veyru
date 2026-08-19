package com.veyru.controller;

import com.veyru.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

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
}
