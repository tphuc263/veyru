package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.like.LikeResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.service.like.LikeService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    // Toggle like - returns full photo state like Facebook/Instagram API
    @PostMapping("/toggle/photo/{photoId}")
    public ResponseEntity<ApiResponse<PhotoResponse>> toggleLike(@PathVariable String photoId) {
        PhotoResponse photo = likeService.toggleLike(photoId);
        return ResponseEntity.ok(ApiResponse.success(photo, "Action completed successfully"));
    }

    // Get all likes for a photo
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<List<LikeResponse>>> getPhotoLikes(@PathVariable String photoId) {
        List<LikeResponse> likes = likeService.getPhotoLikes(photoId);
        return ResponseEntity.ok(ApiResponse.success(likes, "Photo likes retrieved successfully"));
    }

    // Get likes count for a photo
    @GetMapping("/photo/{photoId}/count")
    public ResponseEntity<ApiResponse<Long>> getPhotoLikesCount(@PathVariable String photoId) {
        long count = likeService.getPhotoLikesCount(photoId);
        return ResponseEntity.ok(ApiResponse.success(count, "Photo likes count retrieved successfully"));
    }
}
