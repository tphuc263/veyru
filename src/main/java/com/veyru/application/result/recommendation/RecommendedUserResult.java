package com.veyru.application.result.recommendation;

public record RecommendedUserResult(
    String id,
    String username,
    String imageUrl,
    String bio,
    long followerCount,
    long photoCount,
    double similarityScore,
    String reason) {
  public String getId() { return id; }
  public String getUsername() { return username; }
  public String getImageUrl() { return imageUrl; }
  public String getBio() { return bio; }
  public long getFollowerCount() { return followerCount; }
  public long getPhotoCount() { return photoCount; }
  public double getSimilarityScore() { return similarityScore; }
  public String getReason() { return reason; }
}
