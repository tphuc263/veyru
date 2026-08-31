package com.veyru.domain.model;

import java.time.Instant;

/** Immutable relationship recording that a user liked a comment. */
public record CommentLike(String id, String commentId, String userId, Instant createdAt) {
  public CommentLike {
    if (id != null) id = DomainRules.required(id, "comment-like.id.invalid", "Like ID is invalid");
    commentId =
        DomainRules.required(
            commentId, "comment-like.comment.required", "Liked comment is required");
    userId = DomainRules.required(userId, "comment-like.user.required", "Liking user is required");
    DomainRules.require(createdAt != null, "comment-like.time.required", "Like time is required");
  }

  public static CommentLike create(String commentId, String userId, Instant createdAt) {
    return new CommentLike(null, commentId, userId, createdAt);
  }
}
