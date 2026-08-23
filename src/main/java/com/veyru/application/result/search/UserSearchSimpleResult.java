package com.veyru.application.result.search;

public record UserSearchSimpleResult(String id, String username, String imageUrl) {
  public String getId() { return id; }
  public String getUsername() { return username; }
  public String getImageUrl() { return imageUrl; }
}
