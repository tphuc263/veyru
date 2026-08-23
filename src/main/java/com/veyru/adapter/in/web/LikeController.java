package com.veyru.adapter.in.web;

import com.veyru.application.result.like.LikeResponse;
import com.veyru.application.social.LikeService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
public class LikeController {
  private final LikeService likeService;

  // Toggle like - returns full photo state like Facebook/Instagram API
  @PutMapping("/photos/{photoId}/likes/me")
  public ResponseEntity<Void> like(@PathVariable String photoId) {
    likeService.like(photoId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/photos/{photoId}/likes/me")
  public ResponseEntity<Void> unlike(@PathVariable String photoId) {
    likeService.unlike(photoId);
    return ResponseEntity.noContent().build();
  }

  // Get all likes for a photo
  @GetMapping("/photos/{photoId}/likes")
  public ResponseEntity<List<LikeResponse>> getPhotoLikes(@PathVariable String photoId) {
    List<LikeResponse> likes = likeService.getPhotoLikes(photoId);
    return ResponseEntity.ok(likes);
  }

  // Get likes count for a photo
  @GetMapping("/photos/{photoId}/likes/count")
  public ResponseEntity<Long> getPhotoLikesCount(@PathVariable String photoId) {
    long count = likeService.getPhotoLikesCount(photoId);
    return ResponseEntity.ok(count);
  }

  public LikeController(final LikeService likeService) {
    this.likeService = likeService;
  }
}
