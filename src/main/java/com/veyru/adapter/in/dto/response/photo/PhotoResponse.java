package com.veyru.adapter.in.dto.response.photo;

import java.time.Instant;
import java.util.List;

public record PhotoResponse(
    String id,
    String userId,
    String username,
    String userImageUrl,
    String imageUrl,
    String caption,
    Instant createdAt,
    int likeCount,
    int commentCount,
    int shareCount,
    boolean isLikedByCurrentUser,
    boolean isSavedByCurrentUser,
    List<String> tags) {
  public static PhotoResponse from(com.veyru.application.result.photo.PhotoResult value) {
    return new PhotoResponse(
        value.getId(), value.getUserId(), value.getUsername(), value.getUserImageUrl(),
        value.getImageUrl(), value.getCaption(), value.getCreatedAt(), value.getLikeCount(),
        value.getCommentCount(), value.getShareCount(), value.isLikedByCurrentUser(),
        value.isSavedByCurrentUser(), value.getTags());
  }
}
