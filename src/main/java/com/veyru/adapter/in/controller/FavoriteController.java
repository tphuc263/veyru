package com.veyru.adapter.in.controller;

import com.veyru.application.result.photo.PhotoResponse;
import com.veyru.application.social.FavoriteService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
public class FavoriteController {
  private final FavoriteService favoriteService;

  // Toggle save/unsave a photo
  @PutMapping("/users/me/favorites/{photoId}")
  public ResponseEntity<Void> favorite(@PathVariable String photoId) {
    favoriteService.favorite(photoId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/users/me/favorites/{photoId}")
  public ResponseEntity<Void> unfavorite(@PathVariable String photoId) {
    favoriteService.unfavorite(photoId);
    return ResponseEntity.noContent().build();
  }

  // Get current user's saved photos
  @GetMapping("/users/me/favorites")
  public ResponseEntity<List<PhotoResponse>> getFavorites(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    List<PhotoResponse> favorites = favoriteService.getFavorites(page, size);
    return ResponseEntity.ok(favorites);
  }

  // Check if a photo is saved by the current user
  @GetMapping("/users/me/favorites/{photoId}")
  public ResponseEntity<Boolean> checkFavorite(@PathVariable String photoId) {
    boolean isFavorited = favoriteService.isFavorited(photoId);
    return ResponseEntity.ok(isFavorited);
  }

  public FavoriteController(final FavoriteService favoriteService) {
    this.favoriteService = favoriteService;
  }
}
