package com.veyru.application.result.like;

import java.time.Instant;

public record LikeResult(
    String id, String userId, String username, String userImageUrl, Instant createdAt) {
  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public String getUserImageUrl() {
    return userImageUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
