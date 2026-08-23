package com.veyru.adapter.in.dto.response.like;

import com.veyru.application.result.like.LikeResult;
import java.time.Instant;

public record LikeResponse(
    String id, String userId, String username, String userImageUrl, Instant createdAt) {
  public static LikeResponse from(LikeResult value) {
    return new LikeResponse(
        value.getId(), value.getUserId(), value.getUsername(), value.getUserImageUrl(),
        value.getCreatedAt());
  }
}
