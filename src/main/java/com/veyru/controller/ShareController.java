package com.veyru.controller;

import com.veyru.dto.request.share.SharePhotoRequest;
import com.veyru.dto.response.PageResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.dto.response.share.ShareResponse;
import com.veyru.dto.response.share.ShareWithPhotoResponse;
import com.veyru.service.share.ShareService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/shares")
public class ShareController {
  private final ShareService shareService;

  // Share a photo to current user's profile
  @PostMapping("/photo/{photoId}")
  public ResponseEntity<PhotoResponse> sharePhoto(
      @PathVariable String photoId, @RequestBody(required = false) SharePhotoRequest request) {
    String caption = (request != null) ? request.getCaption() : null;
    PhotoResponse photo = shareService.sharePhoto(photoId, caption);
    return ResponseEntity.status(201).body(photo);
  }

  // Get all shares for a photo
  @GetMapping("/photo/{photoId}")
  public ResponseEntity<List<ShareResponse>> getPhotoShares(@PathVariable String photoId) {
    List<ShareResponse> shares = shareService.getPhotoShares(photoId);
    return ResponseEntity.ok(shares);
  }

  // Get share count for a photo
  @GetMapping("/photo/{photoId}/count")
  public ResponseEntity<Long> getShareCount(@PathVariable String photoId) {
    long count = shareService.getShareCount(photoId);
    return ResponseEntity.ok(count);
  }

  // Check if current user has shared a photo
  @GetMapping("/photo/{photoId}/check")
  public ResponseEntity<Boolean> hasShared(@PathVariable String photoId) {
    boolean shared = shareService.hasShared(photoId);
    return ResponseEntity.ok(shared);
  }

  // Get shares by user ID (for profile page)
  @GetMapping("/user/{userId}")
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
