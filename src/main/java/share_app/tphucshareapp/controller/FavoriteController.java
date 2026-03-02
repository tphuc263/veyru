package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.service.favorite.FavoriteService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // Toggle save/unsave a photo
    @PostMapping("/toggle/{photoId}")
    public ResponseEntity<ApiResponse<PhotoResponse>> toggleFavorite(@PathVariable String photoId) {
        PhotoResponse photo = favoriteService.toggleFavorite(photoId);
        return ResponseEntity.ok(ApiResponse.success(photo, "Favorite toggled successfully"));
    }

    // Get current user's saved photos
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<PhotoResponse>>> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<PhotoResponse> favorites = favoriteService.getFavorites(page, size);
        return ResponseEntity.ok(ApiResponse.success(favorites, "Favorites retrieved successfully"));
    }

    // Check if a photo is saved by the current user
    @GetMapping("/check/{photoId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(@PathVariable String photoId) {
        boolean isFavorited = favoriteService.isFavorited(photoId);
        return ResponseEntity.ok(ApiResponse.success(isFavorited, "Favorite status checked"));
    }
}
