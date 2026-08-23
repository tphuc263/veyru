package com.veyru.application.result.photo;

import java.time.Instant;
import java.util.List;

public record PhotoResult(
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
    boolean likedByCurrentUser,
    boolean savedByCurrentUser,
    List<String> tags) {
  public PhotoResult {
    tags = tags == null ? null : List.copyOf(tags);
  }

  public String getId() { return id; }
  public String getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getUserImageUrl() { return userImageUrl; }
  public String getImageUrl() { return imageUrl; }
  public String getCaption() { return caption; }
  public Instant getCreatedAt() { return createdAt; }
  public int getLikeCount() { return likeCount; }
  public int getCommentCount() { return commentCount; }
  public int getShareCount() { return shareCount; }
  public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
  public boolean isSavedByCurrentUser() { return savedByCurrentUser; }
  public List<String> getTags() { return tags; }
}
