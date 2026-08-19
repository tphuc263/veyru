package com.veyru.dto.request.usertag;

import jakarta.validation.constraints.NotBlank;

public record CreateUserTagRequest(
    @NotBlank(message = "Tagged user ID cannot be blank") String taggedUserId,
    Double positionX,
    Double positionY) {
  public String getTaggedUserId() {
    return taggedUserId;
  }

  public Double getPositionX() {
    return positionX;
  }

  public Double getPositionY() {
    return positionY;
  }
}
