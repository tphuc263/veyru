package com.veyru.adapter.in.dto.response.recommendation;

import com.veyru.application.result.recommendation.RecommendedUserResult;

public record RecommendedUserResponse(
    String id,
    String username,
    String imageUrl,
    String bio,
    long followerCount,
    long photoCount,
    double similarityScore,
    String reason) {
  public static RecommendedUserResponse from(RecommendedUserResult value) {
    return new RecommendedUserResponse(
        value.getId(), value.getUsername(), value.getImageUrl(), value.getBio(),
        value.getFollowerCount(), value.getPhotoCount(), value.getSimilarityScore(),
        value.getReason());
  }
}
