package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.CommentLike;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comment_likes")
final class CommentLikeDocument {
  @Id private String id;
  private String commentId;
  private String userId;
  private Instant createdAt;

  static CommentLikeDocument fromDomain(CommentLike like) {
    CommentLikeDocument document = new CommentLikeDocument();
    document.id = like.id();
    document.commentId = like.commentId();
    document.userId = like.userId();
    document.createdAt = like.createdAt();
    return document;
  }

  CommentLike toDomain() {
    return new CommentLike(id, commentId, userId, createdAt);
  }
}
