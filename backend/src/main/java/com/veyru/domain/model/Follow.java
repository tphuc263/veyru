package com.veyru.domain.model;

import java.time.Instant;

/** Immutable directed relationship between two distinct users. */
public record Follow(String id, String followerId, String followingId, Instant createdAt) {
  public Follow {
    if (id != null) id = DomainRules.required(id, "follow.id.invalid", "Follow ID is invalid");
    followerId =
        DomainRules.required(followerId, "follow.follower.required", "Follower is required");
    followingId =
        DomainRules.required(
            followingId, "follow.following.required", "Following user is required");
    DomainRules.require(
        !followerId.equals(followingId), "follow.self.invalid", "A user cannot follow themselves");
    DomainRules.require(createdAt != null, "follow.time.required", "Follow time is required");
  }

  public static Follow create(String followerId, String followingId, Instant createdAt) {
    return new Follow(null, followerId, followingId, createdAt);
  }
}
