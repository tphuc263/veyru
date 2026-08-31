package com.veyru.domain.model;

import java.time.Instant;

/**
 * Immutable position-aware tag value embedded in a photo. Coordinates use the normalized [0, 1]
 * range.
 */
public record PhotoUserTag(
    String taggedUserId,
    String taggedByUserId,
    String username,
    Double positionX,
    Double positionY,
    Instant createdAt) {
  public PhotoUserTag {
    taggedUserId =
        DomainRules.required(taggedUserId, "photo-tag.user.required", "Tagged user is required");
    taggedByUserId =
        DomainRules.required(taggedByUserId, "photo-tag.actor.required", "Tag actor is required");
    username =
        DomainRules.required(
            username, "photo-tag.username.required", "Tagged username is required");
    DomainRules.require(createdAt != null, "photo-tag.time.required", "Tag time is required");
    DomainRules.require(
        (positionX == null) == (positionY == null),
        "photo-tag.position.incomplete",
        "Tag coordinates must be provided together");
    if (positionX != null) {
      DomainRules.require(
          positionX >= 0 && positionX <= 1 && positionY >= 0 && positionY <= 1,
          "photo-tag.position.out-of-range",
          "Tag coordinates must be between 0 and 1");
    }
  }
}
