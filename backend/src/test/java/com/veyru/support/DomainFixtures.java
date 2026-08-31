package com.veyru.support;

import com.veyru.domain.enums.UserRole;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import com.veyru.domain.model.UserSnapshot;
import java.time.Instant;
import java.util.List;

public final class DomainFixtures {
  private DomainFixtures() {}

  public static User user(String id) {
    return user(id, 0, 0, 0);
  }

  public static User user(String id, long photoCount, long followerCount, long followingCount) {
    return User.restore(
        id,
        id,
        id + "@example.com",
        null,
        "hash",
        UserRole.ROLE_USER,
        null,
        null,
        Instant.EPOCH,
        photoCount,
        followerCount,
        followingCount,
        null,
        null);
  }

  public static Photo photo(String id, String authorId, Instant createdAt) {
    return Photo.restore(
        id,
        "https://example.test/photo.png",
        "caption",
        createdAt,
        List.of("tag"),
        new UserSnapshot(authorId, authorId),
        0,
        0,
        0,
        List.of());
  }
}
