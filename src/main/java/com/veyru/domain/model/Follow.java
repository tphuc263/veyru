package com.veyru.domain.model;

import java.time.Instant;

public class Follow {
  private String id;
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





  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Follow)) return false;
    final Follow other = (Follow) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$followerId = this.getFollowerId();
    final Object other$followerId = other.getFollowerId();
    if (this$followerId == null
        ? other$followerId != null
        : !this$followerId.equals(other$followerId)) return false;
    final Object this$followingId = this.getFollowingId();
    final Object other$followingId = other.getFollowingId();
    if (this$followingId == null
        ? other$followingId != null
        : !this$followingId.equals(other$followingId)) return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Follow;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $followerId = this.getFollowerId();
    result = result * PRIME + ($followerId == null ? 43 : $followerId.hashCode());
    final Object $followingId = this.getFollowingId();
    result = result * PRIME + ($followingId == null ? 43 : $followingId.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    return result;
  }

  @Override
  public String toString() {
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

  public static Follow create(String followerId, String followingId, Instant createdAt) {
    if (followerId == null || followerId.isBlank() || followingId == null || followingId.isBlank()) {
      throw new IllegalArgumentException("Follow participants are required");
    }
    if (followerId.equals(followingId)) {
      throw new IllegalArgumentException("A user cannot follow themselves");
    }
    Follow follow = new Follow();
    follow.followerId = followerId;
    follow.followingId = followingId;
    follow.createdAt = createdAt;
    return follow;
  }
}
