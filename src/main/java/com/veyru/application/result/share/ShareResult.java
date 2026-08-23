package com.veyru.application.result.share;

import java.time.Instant;

public final class ShareResult {
  private final String id;
  private final String photoId;
  private final String userId;
  private final String username;
  private final String userImageUrl;
  private final String caption;
  private final Instant createdAt;

  public ShareResult(
      String id,
      String photoId,
      String userId,
      String username,
      String userImageUrl,
      String caption,
      Instant createdAt) {
    this.id = id;
    this.photoId = photoId;
    this.userId = userId;
    this.username = username;
    this.userImageUrl = userImageUrl;
    this.caption = caption;
    this.createdAt = createdAt;
  }

  public String getId() { return id; }
  public String getPhotoId() { return photoId; }
  public String getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getUserImageUrl() { return userImageUrl; }
  public String getCaption() { return caption; }
  public Instant getCreatedAt() { return createdAt; }
}
