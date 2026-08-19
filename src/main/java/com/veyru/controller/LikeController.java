package com.veyru.controller;

import com.veyru.dto.response.like.LikeResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.service.like.LikeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/likes")
@RequiredArgsConstructor
public class LikeController {
  private final LikeService likeService;

  // Toggle like - returns full photo state like Facebook/Instagram API
  @PostMapping("/toggle/photo/{photoId}")
  public ResponseEntity<PhotoResponse> toggleLike(@PathVariable String photoId) {
    PhotoResponse photo = likeService.toggleLike(photoId);
    return ResponseEntity.ok(photo);
  }

  // Get all likes for a photo
  @GetMapping("/photo/{photoId}")
  public ResponseEntity<List<LikeResponse>> getPhotoLikes(@PathVariable String photoId) {
    List<LikeResponse> likes = likeService.getPhotoLikes(photoId);
    return ResponseEntity.ok(likes);
  }

  // Get likes count for a photo
  @GetMapping("/photo/{photoId}/count")
  public ResponseEntity<Long> getPhotoLikesCount(@PathVariable String photoId) {
    long count = likeService.getPhotoLikesCount(photoId);
    return ResponseEntity.ok(count);
  }
}
