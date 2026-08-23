package com.veyru.application.result.user;

import java.util.Map;

public final class UserProfileResult {
  private final String id;
  private final String username;
  private final String imageUrl;
  private final Map<String, Long> stats;
  private final String bio;
  private final boolean followingByCurrentUser;

  public UserProfileResult(
      String id,
      String username,
      String imageUrl,
      Map<String, Long> stats,
      String bio,
      boolean followingByCurrentUser) {
    this.id = id;
    this.username = username;
    this.imageUrl = imageUrl;
    this.stats = stats == null ? null : Map.copyOf(stats);
    this.bio = bio;
    this.followingByCurrentUser = followingByCurrentUser;
  }

  public String getId() { return id; }
  public String getUsername() { return username; }
  public String getImageUrl() { return imageUrl; }
  public Map<String, Long> getStats() { return stats; }
  public String getBio() { return bio; }
  public boolean isFollowingByCurrentUser() { return followingByCurrentUser; }
}
