package com.veyru.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comment_likes")
@CompoundIndex(name = "comment_user_idx", def = "{\'commentId\': 1, \'userId\': 1}", unique = true)
public class CommentLike {
  @Id private String id;
  @Indexed private String commentId;
  private String userId;
  private Instant createdAt;

  public String getId() {
    return this.id;
  }

  public String getCommentId() {
    return this.commentId;
  }

  public String getUserId() {
    return this.userId;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setCommentId(final String commentId) {
    this.commentId = commentId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof CommentLike)) return false;
    final CommentLike other = (CommentLike) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$commentId = this.getCommentId();
    final java.lang.Object other$commentId = other.getCommentId();
    if (this$commentId == null ? other$commentId != null : !this$commentId.equals(other$commentId))
      return false;
    final java.lang.Object this$userId = this.getUserId();
    final java.lang.Object other$userId = other.getUserId();
    if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
      return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof CommentLike;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $commentId = this.getCommentId();
    result = result * PRIME + ($commentId == null ? 43 : $commentId.hashCode());
    final java.lang.Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "CommentLike(id="
        + this.getId()
        + ", commentId="
        + this.getCommentId()
        + ", userId="
        + this.getUserId()
        + ", createdAt="
        + this.getCreatedAt()
        + ")";
  }

  public CommentLike() {}

  public CommentLike(
      final String id, final String commentId, final String userId, final Instant createdAt) {
    this.id = id;
    this.commentId = commentId;
    this.userId = userId;
    this.createdAt = createdAt;
  }
}
