package com.veyru.application.result.user;

import java.util.Map;

public record UserProfileResult(
    String id,
    String username,
    String imageUrl,
    Map<String, Long> stats,
    String bio,
    boolean followingByCurrentUser) {
  public UserProfileResult {
    stats = stats == null ? null : Map.copyOf(stats);
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public Map<String, Long> getStats() {
    return stats;
  }

  public String getBio() {
    return bio;
  }

  public boolean isFollowingByCurrentUser() {
    return followingByCurrentUser;
  }
}
