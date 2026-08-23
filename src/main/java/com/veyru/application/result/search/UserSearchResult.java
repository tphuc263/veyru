package com.veyru.application.result.search;

public record UserSearchResult(
    String id,
    String username,
    String firstName,
    String lastName,
    String imageUrl,
    String bio,
    long followersCount,
    boolean followedByCurrentUser,
    double searchScore) {
  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getBio() {
    return bio;
  }

  public long getFollowersCount() {
    return followersCount;
  }

  public boolean isFollowedByCurrentUser() {
    return followedByCurrentUser;
  }

  public double getSearchScore() {
    return searchScore;
  }
}
