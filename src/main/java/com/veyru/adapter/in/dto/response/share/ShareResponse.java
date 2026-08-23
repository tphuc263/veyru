package com.veyru.adapter.in.dto.response.share;

import com.veyru.application.result.share.ShareResult;
import java.time.Instant;

public record ShareResponse(
    String id,
    String photoId,
    String userId,
    String username,
    String userImageUrl,
    String caption,
    Instant createdAt) {
  public static ShareResponse from(ShareResult value) {
    return new ShareResponse(
        value.getId(),
        value.getPhotoId(),
        value.getUserId(),
        value.getUsername(),
        value.getUserImageUrl(),
        value.getCaption(),
        value.getCreatedAt());
  }
}
