package com.veyru.adapter.in.dto.response.usertag;

import com.veyru.application.result.usertag.UserTagResult;
import java.time.Instant;

public record UserTagResponse(
    String id,
    String photoId,
    String taggedUserId,
    String taggedByUserId,
    String username,
    String userImageUrl,
    Double positionX,
    Double positionY,
    Instant createdAt) {
  public static UserTagResponse from(UserTagResult value) {
    return new UserTagResponse(
        value.getId(),
        value.getPhotoId(),
        value.getTaggedUserId(),
        value.getTaggedByUserId(),
        value.getUsername(),
        value.getUserImageUrl(),
        value.getPositionX(),
        value.getPositionY(),
        value.getCreatedAt());
  }
}
