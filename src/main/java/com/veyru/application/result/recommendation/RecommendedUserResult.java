package com.veyru.application.result.recommendation;

public final class RecommendedUserResult {
  private final String id;
  private final String username;
  private final String imageUrl;
  private final String bio;
  private final long followerCount;
  private final long photoCount;
  private final double similarityScore;
  private final String reason;

  public RecommendedUserResult(
      String id,
      String username,
      String imageUrl,
      String bio,
      long followerCount,
      long photoCount,
      double similarityScore,
      String reason) {
    this.id = id;
    this.username = username;
    this.imageUrl = imageUrl;
    this.bio = bio;
    this.followerCount = followerCount;
    this.photoCount = photoCount;
    this.similarityScore = similarityScore;
    this.reason = reason;
  }

  public String getId() { return id; }
  public String getUsername() { return username; }
  public String getImageUrl() { return imageUrl; }
  public String getBio() { return bio; }
  public long getFollowerCount() { return followerCount; }
  public long getPhotoCount() { return photoCount; }
  public double getSimilarityScore() { return similarityScore; }
  public String getReason() { return reason; }
}
