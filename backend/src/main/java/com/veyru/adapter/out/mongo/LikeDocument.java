package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.Like;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "likes")
final class LikeDocument {
  @Id private String id;
  private String photoId;
  private String userId;
  private Instant createdAt;

  static LikeDocument fromDomain(Like like) {
    LikeDocument document = new LikeDocument();
    document.id = like.id();
    document.photoId = like.photoId();
    document.userId = like.userId();
    document.createdAt = like.createdAt();
    return document;
  }

  Like toDomain() {
    return new Like(id, photoId, userId, createdAt);
  }
}
