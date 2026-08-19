package com.veyru.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "follows")
public class Follow {
  @Id private String id;
  private String followerId;
  private String followingId;
  private Instant createdAt;

  public String getId() {
    return this.id;
  }

  public String getFollowerId() {
    return this.followerId;
  }

  public String getFollowingId() {
    return this.followingId;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setFollowerId(final String followerId) {
    this.followerId = followerId;
  }

  public void setFollowingId(final String followingId) {
    this.followingId = followingId;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof Follow)) return false;
    final Follow other = (Follow) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$followerId = this.getFollowerId();
    final java.lang.Object other$followerId = other.getFollowerId();
    if (this$followerId == null
        ? other$followerId != null
        : !this$followerId.equals(other$followerId)) return false;
    final java.lang.Object this$followingId = this.getFollowingId();
    final java.lang.Object other$followingId = other.getFollowingId();
    if (this$followingId == null
        ? other$followingId != null
        : !this$followingId.equals(other$followingId)) return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof Follow;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $followerId = this.getFollowerId();
    result = result * PRIME + ($followerId == null ? 43 : $followerId.hashCode());
    final java.lang.Object $followingId = this.getFollowingId();
    result = result * PRIME + ($followingId == null ? 43 : $followingId.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "Follow(id="
        + this.getId()
        + ", followerId="
        + this.getFollowerId()
        + ", followingId="
        + this.getFollowingId()
        + ", createdAt="
        + this.getCreatedAt()
        + ")";
  }

  public Follow() {}

  public Follow(
      final String id, final String followerId, final String followingId, final Instant createdAt) {
    this.id = id;
    this.followerId = followerId;
    this.followingId = followingId;
    this.createdAt = createdAt;
  }
}
