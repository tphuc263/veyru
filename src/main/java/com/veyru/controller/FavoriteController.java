package com.veyru.controller;

import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.service.favorite.FavoriteService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/favorites")
public class FavoriteController {
  private final FavoriteService favoriteService;

  // Toggle save/unsave a photo
  @PostMapping("/toggle/{photoId}")
  public ResponseEntity<PhotoResponse> toggleFavorite(@PathVariable String photoId) {
    PhotoResponse photo = favoriteService.toggleFavorite(photoId);
    return ResponseEntity.ok(photo);
  }

  // Get current user's saved photos
  @GetMapping("/me")
  public ResponseEntity<List<PhotoResponse>> getFavorites(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    List<PhotoResponse> favorites = favoriteService.getFavorites(page, size);
    return ResponseEntity.ok(favorites);
  }

  // Check if a photo is saved by the current user
  @GetMapping("/check/{photoId}")
  public ResponseEntity<Boolean> checkFavorite(@PathVariable String photoId) {
    boolean isFavorited = favoriteService.isFavorited(photoId);
    return ResponseEntity.ok(isFavorited);
  }

  public FavoriteController(final FavoriteService favoriteService) {
    this.favoriteService = favoriteService;
  }
}
