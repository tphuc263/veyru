package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.application.common.PageResult;
import com.veyru.application.discovery.UnifiedPostService;
import com.veyru.application.result.post.UnifiedPostResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/posts")
public class PostController {
  private final UnifiedPostService unifiedPostService;

  /** Get unified posts (photos + shares) for a user profile */
  @GetMapping
  public ResponseEntity<PageResponse<UnifiedPostResponse>> getUserPosts(
      @RequestParam String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    PageResult<UnifiedPostResponse> posts = unifiedPostService.getUserPosts(userId, page, size);
    return ResponseEntity.ok(PageResponse.from(posts));
  }

  public PostController(final UnifiedPostService unifiedPostService) {
    this.unifiedPostService = unifiedPostService;
  }
}
