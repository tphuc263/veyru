package com.veyru.adapter.in.dto.response.share;

import com.veyru.application.result.share.ShareWithPhotoResult;
import java.time.Instant;

public record ShareWithPhotoResponse(
    String id,
    String photoId,
    String userId,
    String username,
    String userImageUrl,
    String caption,
    Instant createdAt,
    String originalPhotoId,
    String originalImageUrl,
    String originalCaption,
    String originalUsername,
    String originalUserImageUrl,
    Instant originalCreatedAt,
    int originalLikeCount,
    int originalCommentCount,
    int originalShareCount) {
  public static ShareWithPhotoResponse from(ShareWithPhotoResult value) {
    return new ShareWithPhotoResponse(
        value.getId(), value.getPhotoId(), value.getUserId(), value.getUsername(),
        value.getUserImageUrl(), value.getCaption(), value.getCreatedAt(),
        value.getOriginalPhotoId(), value.getOriginalImageUrl(), value.getOriginalCaption(),
        value.getOriginalUsername(), value.getOriginalUserImageUrl(), value.getOriginalCreatedAt(),
        value.getOriginalLikeCount(), value.getOriginalCommentCount(), value.getOriginalShareCount());
  }
}
