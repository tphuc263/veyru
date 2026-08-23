package com.veyru.application.result.follow;

public record FollowResult(
    String id,
    String userId,
    String username,
    String userImageUrl,
    String firstName,
    String lastName,
    String bio,
    boolean followedByCurrentUser) {
  public String getId() { return id; }
  public String getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getUserImageUrl() { return userImageUrl; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getBio() { return bio; }
  public boolean isFollowedByCurrentUser() { return followedByCurrentUser; }
}
