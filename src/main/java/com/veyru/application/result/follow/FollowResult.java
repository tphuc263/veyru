package com.veyru.application.result.follow;

public final class FollowResult {
  private final String id;
  private final String userId;
  private final String username;
  private final String userImageUrl;
  private final String firstName;
  private final String lastName;
  private final String bio;
  private final boolean followedByCurrentUser;

  public FollowResult(
      String id,
      String userId,
      String username,
      String userImageUrl,
      String firstName,
      String lastName,
      String bio,
      boolean followedByCurrentUser) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.userImageUrl = userImageUrl;
    this.firstName = firstName;
    this.lastName = lastName;
    this.bio = bio;
    this.followedByCurrentUser = followedByCurrentUser;
  }

  public String getId() { return id; }
  public String getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getUserImageUrl() { return userImageUrl; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getBio() { return bio; }
  public boolean isFollowedByCurrentUser() { return followedByCurrentUser; }
}
