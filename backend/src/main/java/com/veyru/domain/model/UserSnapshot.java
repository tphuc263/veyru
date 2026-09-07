package com.veyru.domain.model;

/** Immutable display snapshot embedded in content so reads do not require an author lookup. */
public record UserSnapshot(String userId, String username) {
  public UserSnapshot {
    userId = DomainRules.required(userId, "user-snapshot.id.required", "User ID is required");
    username =
        DomainRules.required(username, "user-snapshot.username.required", "Username is required");
  }
}
