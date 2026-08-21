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

  public TagController(final TagService tagService) {
    this.tagService = tagService;
  }
}
