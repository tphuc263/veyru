package com.veyru.application.result.share;

import java.time.Instant;

public record ShareResult(
    String id,
    String photoId,
    String userId,
    String username,
    String userImageUrl,
    String caption,
    Instant createdAt) {
  public String getId() { return id; }
  public String getPhotoId() { return photoId; }
  public String getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getUserImageUrl() { return userImageUrl; }
  public String getCaption() { return caption; }
  public Instant getCreatedAt() { return createdAt; }
}
