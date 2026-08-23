package com.veyru.adapter.in.web;

import com.veyru.application.tag.TagQueryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/tags")
public class TagController {
  private final TagQueryService tagService;

  @GetMapping
  public ResponseEntity<List<String>> getHashtags(
      @RequestParam(defaultValue = "trending") String sort,
      @RequestParam(defaultValue = "10") int limit) {
    List<String> hashtags =
        switch (sort) {
          case "trending" -> tagService.getTrendingHashtags(limit);
          case "popular" -> tagService.getPopularHashtags(limit);
          default -> throw new IllegalArgumentException("Unsupported tag sort");
        };
    List<String> formattedHashtags = hashtags.stream().map(tag -> "#" + tag).toList();
    return ResponseEntity.ok(formattedHashtags);
  }

  public TagController(final TagQueryService tagService) {
    this.tagService = tagService;
  }
}
