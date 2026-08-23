package com.veyru.application.result.usertag;

import java.time.Instant;

public record UserTagResult(
    String id,
    String photoId,
    String taggedUserId,
    String taggedByUserId,
    String username,
    String userImageUrl,
    Double positionX,
    Double positionY,
    Instant createdAt) {
  public String getId() { return id; }
  public String getPhotoId() { return photoId; }
  public String getTaggedUserId() { return taggedUserId; }
  public String getTaggedByUserId() { return taggedByUserId; }
  public String getUsername() { return username; }
  public String getUserImageUrl() { return userImageUrl; }
  public Double getPositionX() { return positionX; }
  public Double getPositionY() { return positionY; }
  public Instant getCreatedAt() { return createdAt; }
}
