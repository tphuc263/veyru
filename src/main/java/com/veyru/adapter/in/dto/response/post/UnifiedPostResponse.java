package com.veyru.adapter.in.dto.response.post;

import com.veyru.application.result.post.UnifiedPostResult;
import java.time.Instant;

public record UnifiedPostResponse(
    String id,
    UnifiedPostResult.PostType type,
    Instant createdAt,
    String userId,
    String username,
    String userImageUrl,
    String imageUrl,
    String caption,
    int likeCount,
    int commentCount,
    int shareCount,
    boolean isLikedByCurrentUser,
    boolean isSavedByCurrentUser,
    String shareCaption,
    String originalPhotoId,
    String originalImageUrl,
    String originalCaption,
    String originalUsername,
    String originalUserImageUrl,
    Instant originalCreatedAt,
    int originalLikeCount,
    int originalCommentCount,
    int originalShareCount) {
  public static UnifiedPostResponse from(UnifiedPostResult value) {
    return new UnifiedPostResponse(
        value.getId(), value.getType(), value.getCreatedAt(), value.getUserId(), value.getUsername(),
        value.getUserImageUrl(), value.getImageUrl(), value.getCaption(), value.getLikeCount(),
        value.getCommentCount(), value.getShareCount(), value.isLikedByCurrentUser(),
        value.isSavedByCurrentUser(), value.getShareCaption(), value.getOriginalPhotoId(),
        value.getOriginalImageUrl(), value.getOriginalCaption(), value.getOriginalUsername(),
        value.getOriginalUserImageUrl(), value.getOriginalCreatedAt(), value.getOriginalLikeCount(),
        value.getOriginalCommentCount(), value.getOriginalShareCount());
  }
}
