package com.veyru.adapter.in.dto.response.follow;

import com.veyru.application.result.follow.FollowResult;

public record FollowResponse(
    String id,
    String userId,
    String username,
    String userImageUrl,
    String firstName,
    String lastName,
    String bio,
    boolean isFollowedByCurrentUser) {
  public static FollowResponse from(FollowResult value) {
    return new FollowResponse(
        value.getId(),
        value.getUserId(),
        value.getUsername(),
        value.getUserImageUrl(),
        value.getFirstName(),
        value.getLastName(),
        value.getBio(),
        value.isFollowedByCurrentUser());
  }
}
