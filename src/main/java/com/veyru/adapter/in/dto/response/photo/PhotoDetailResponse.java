package com.veyru.adapter.in.dto.response.photo;

import com.veyru.adapter.in.dto.response.comment.CommentResponse;
import com.veyru.adapter.in.dto.response.like.LikeResponse;
import com.veyru.application.result.photo.PhotoDetailResult;
import java.time.Instant;
import java.util.List;

public record PhotoDetailResponse(
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
    List<LikeResponse> likes,
    List<CommentResponse> comments,
    List<String> tags) {
  public static PhotoDetailResponse from(PhotoDetailResult value) {
    return new PhotoDetailResponse(
        value.getId(),
        value.getUserId(),
        value.getUsername(),
        value.getUserImageUrl(),
        value.getImageUrl(),
        value.getCaption(),
        value.getCreatedAt(),
        value.getLikeCount(),
        value.getCommentCount(),
        value.getShareCount(),
        value.isLikedByCurrentUser(),
        value.isSavedByCurrentUser(),
        value.getLikes().stream().map(LikeResponse::from).toList(),
        value.getComments().stream().map(CommentResponse::from).toList(),
        value.getTags());
  }
}
