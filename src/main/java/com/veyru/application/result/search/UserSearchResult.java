package com.veyru.application.result.search;

public final class UserSearchResult {
  private final String id;
  private final String username;
  private final String firstName;
  private final String lastName;
  private final String imageUrl;
  private final String bio;
  private final long followersCount;
  private final boolean followedByCurrentUser;
  private final double searchScore;

  public UserSearchResult(
      String id,
      String username,
      String firstName,
      String lastName,
      String imageUrl,
      String bio,
      long followersCount,
      boolean followedByCurrentUser,
      double searchScore) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.imageUrl = imageUrl;
    this.bio = bio;
    this.followersCount = followersCount;
    this.followedByCurrentUser = followedByCurrentUser;
    this.searchScore = searchScore;
  }

  public String getId() { return id; }
  public String getUsername() { return username; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getImageUrl() { return imageUrl; }
  public String getBio() { return bio; }
  public long getFollowersCount() { return followersCount; }
  public boolean isFollowedByCurrentUser() { return followedByCurrentUser; }
  public double getSearchScore() { return searchScore; }
}
