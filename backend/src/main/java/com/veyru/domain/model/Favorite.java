package com.veyru.domain.model;

import java.time.Instant;

/** Immutable relationship recording that a user saved a photo. */
public record Favorite(String id, String userId, String photoId, Instant createdAt) {
  public Favorite {
    if (id != null) id = DomainRules.required(id, "favorite.id.invalid", "Favorite ID is invalid");
    userId = DomainRules.required(userId, "favorite.user.required", "Saving user is required");
    photoId = DomainRules.required(photoId, "favorite.photo.required", "Saved photo is required");
    DomainRules.require(createdAt != null, "favorite.time.required", "Favorite time is required");
  }

  public static Favorite create(String userId, String photoId, Instant createdAt) {
    return new Favorite(null, userId, photoId, createdAt);
  }
}
