package com.veyru.controller;

import com.veyru.dto.response.PageResponse;
import com.veyru.dto.response.post.UnifiedPostResponse;
import com.veyru.service.post.UnifiedPostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/posts")
public class PostController {
  private final UnifiedPostService unifiedPostService;

  /** Get unified posts (photos + shares) for a user profile */
  @GetMapping("/user/{userId}")
  public ResponseEntity<PageResponse<UnifiedPostResponse>> getUserPosts(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<UnifiedPostResponse> posts = unifiedPostService.getUserPosts(userId, page, size);
    return ResponseEntity.ok(PageResponse.from(posts));
  }

  public PostController(final UnifiedPostService unifiedPostService) {
    this.unifiedPostService = unifiedPostService;
  }
}
