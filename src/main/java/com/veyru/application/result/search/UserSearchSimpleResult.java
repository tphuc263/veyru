package com.veyru.application.result.search;

public final class UserSearchSimpleResult {
  private final String id;
  private final String username;
  private final String imageUrl;

  public UserSearchSimpleResult(String id, String username, String imageUrl) {
    this.id = id;
    this.username = username;
    this.imageUrl = imageUrl;
  }

  public String getId() { return id; }
  public String getUsername() { return username; }
  public String getImageUrl() { return imageUrl; }
}
