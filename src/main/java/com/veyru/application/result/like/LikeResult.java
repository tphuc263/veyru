package com.veyru.application.result.like;

import java.time.Instant;

public final class LikeResult {
  private final String id;
  private final String userId;
  private final String username;
  private final String userImageUrl;
  private final Instant createdAt;

  public LikeResult(
      String id, String userId, String username, String userImageUrl, Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.userImageUrl = userImageUrl;
    this.createdAt = createdAt;
  }

  public String getId() { return id; }
  public String getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getUserImageUrl() { return userImageUrl; }
  public Instant getCreatedAt() { return createdAt; }
}
