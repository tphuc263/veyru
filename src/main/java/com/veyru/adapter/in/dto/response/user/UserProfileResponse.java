package com.veyru.adapter.in.dto.response.user;

import com.veyru.application.result.user.UserProfileResult;
import java.util.Map;

public record UserProfileResponse(
    String id,
    String username,
    String imageUrl,
    Map<String, Long> stats,
    String bio,
    boolean isFollowingByCurrentUser) {
  public static UserProfileResponse from(UserProfileResult value) {
    return new UserProfileResponse(
        value.getId(),
        value.getUsername(),
        value.getImageUrl(),
        value.getStats(),
        value.getBio(),
        value.isFollowingByCurrentUser());
  }
}
