package com.veyru.adapter.in.dto.response.search;

import com.veyru.application.result.search.UserSearchResult;

public record UserSearchResponse(
    String id,
    String username,
    String firstName,
    String lastName,
    String imageUrl,
    String bio,
    long followersCount,
    boolean isFollowedByCurrentUser,
    double searchScore) {
  public static UserSearchResponse from(UserSearchResult value) {
    return new UserSearchResponse(
        value.getId(),
        value.getUsername(),
        value.getFirstName(),
        value.getLastName(),
        value.getImageUrl(),
        value.getBio(),
        value.getFollowersCount(),
        value.isFollowedByCurrentUser(),
        value.getSearchScore());
  }
}
