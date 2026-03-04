package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.share.SharePhotoRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.share.ShareResponse;
import share_app.tphucshareapp.service.share.ShareService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/shares")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    // Share a photo to current user's profile
    @PostMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<PhotoResponse>> sharePhoto(
            @PathVariable String photoId,
            @RequestBody(required = false) SharePhotoRequest request) {
        String caption = (request != null) ? request.getCaption() : null;
        PhotoResponse photo = shareService.sharePhoto(photoId, caption);
        return ResponseEntity.ok(ApiResponse.success(photo, "Photo shared successfully"));
    }

    // Get all shares for a photo
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<List<ShareResponse>>> getPhotoShares(
            @PathVariable String photoId) {
        List<ShareResponse> shares = shareService.getPhotoShares(photoId);
        return ResponseEntity.ok(ApiResponse.success(shares, "Photo shares retrieved successfully"));
    }

    // Get share count for a photo
    @GetMapping("/photo/{photoId}/count")
    public ResponseEntity<ApiResponse<Long>> getShareCount(@PathVariable String photoId) {
        long count = shareService.getShareCount(photoId);
        return ResponseEntity.ok(ApiResponse.success(count, "Share count retrieved successfully"));
    }

    // Check if current user has shared a photo
    @GetMapping("/photo/{photoId}/check")
    public ResponseEntity<ApiResponse<Boolean>> hasShared(@PathVariable String photoId) {
        boolean shared = shareService.hasShared(photoId);
        return ResponseEntity.ok(ApiResponse.success(shared, "Share status retrieved"));
    }
}
