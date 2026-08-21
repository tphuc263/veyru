package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.request.share.SharePhotoRequest;
import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.adapter.in.dto.response.share.ShareResponse;
import com.veyru.adapter.in.dto.response.share.ShareWithPhotoResponse;
import com.veyru.domain.service.share.ShareService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
public class ShareController {
  private final ShareService shareService;

  // Share a photo to current user's profile
  @PostMapping("/photos/{photoId}/shares")
  public ResponseEntity<PhotoResponse> sharePhoto(
      @PathVariable String photoId, @RequestBody(required = false) SharePhotoRequest request) {
    String caption = (request != null) ? request.getCaption() : null;
    PhotoResponse photo = shareService.sharePhoto(photoId, caption);
    return ResponseEntity.status(201).body(photo);
  }

  // Get all shares for a photo
  @GetMapping("/photos/{photoId}/shares")
  public ResponseEntity<List<ShareResponse>> getPhotoShares(@PathVariable String photoId) {
    List<ShareResponse> shares = shareService.getPhotoShares(photoId);
    return ResponseEntity.ok(shares);
  }

  // Get share count for a photo
  @GetMapping("/photos/{photoId}/shares/count")
  public ResponseEntity<Long> getShareCount(@PathVariable String photoId) {
    long count = shareService.getShareCount(photoId);
    return ResponseEntity.ok(count);
  }

  // Check if current user has shared a photo
  @GetMapping("/photos/{photoId}/shares/me")
  public ResponseEntity<Boolean> hasShared(@PathVariable String photoId) {
    boolean shared = shareService.hasShared(photoId);
    return ResponseEntity.ok(shared);
  }

  // Get shares by user ID (for profile page)
  @GetMapping("/users/{userId}/shares")
  public ResponseEntity<PageResponse<ShareWithPhotoResponse>> getUserShares(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<ShareWithPhotoResponse> shares = shareService.getSharesByUserId(userId, page, size);
    return ResponseEntity.ok(PageResponse.from(shares));
  }

  public ShareController(final ShareService shareService) {
    this.shareService = shareService;
  }
}
