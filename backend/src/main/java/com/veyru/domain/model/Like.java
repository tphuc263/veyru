package com.veyru.domain.model;

import java.time.Instant;

/** Immutable relationship recording that a user liked a photo. */
public record Like(String id, String photoId, String userId, Instant createdAt) {
  public Like {
    if (id != null) id = DomainRules.required(id, "like.id.invalid", "Like ID is invalid");
    photoId = DomainRules.required(photoId, "like.photo.required", "Liked photo is required");
    userId = DomainRules.required(userId, "like.user.required", "Liking user is required");
    DomainRules.require(createdAt != null, "like.time.required", "Like time is required");
  }

  public static Like create(String photoId, String userId, Instant createdAt) {
    return new Like(null, photoId, userId, createdAt);
  }
}
