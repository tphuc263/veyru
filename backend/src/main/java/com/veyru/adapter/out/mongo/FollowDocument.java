package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.Follow;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "follows")
final class FollowDocument {
  @Id private String id;
  private String followerId;
  private String followingId;
  private Instant createdAt;

  static FollowDocument fromDomain(Follow follow) {
    FollowDocument document = new FollowDocument();
    document.id = follow.id();
    document.followerId = follow.followerId();
    document.followingId = follow.followingId();
    document.createdAt = follow.createdAt();
    return document;
  }

  Follow toDomain() {
    return new Follow(id, followerId, followingId, createdAt);
  }
}
