package com.veyru.domain.model;

import java.time.Instant;

/** Immutable relationship representing a photo shared onto a user's profile. */
public record Share(String id, String photoId, String userId, String caption, Instant createdAt) {
  public Share {
    if (id != null) id = DomainRules.required(id, "share.id.invalid", "Share ID is invalid");
    photoId = DomainRules.required(photoId, "share.photo.required", "Shared photo is required");
    userId = DomainRules.required(userId, "share.user.required", "Sharing user is required");
    caption =
        DomainRules.limited(caption, 2_200, "share.caption.too-long", "Share caption is too long");
    DomainRules.require(createdAt != null, "share.time.required", "Share time is required");
  }

  public static Share create(String photoId, String userId, String caption, Instant createdAt) {
    return new Share(null, photoId, userId, caption, createdAt);
  }
}
