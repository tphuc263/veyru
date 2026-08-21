package com.veyru.domain.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "favorites")
@CompoundIndex(name = "user_photo_unique", def = "{\'userId\': 1, \'photoId\': 1}", unique = true)
public class Favorite {
  @Id private String id;
  private String userId;
  private String photoId;
  private Instant createdAt;

  public String getId() {
    return this.id;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getPhotoId() {
    return this.photoId;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setPhotoId(final String photoId) {
    this.photoId = photoId;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Favorite)) return false;
    final Favorite other = (Favorite) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$userId = this.getUserId();
    final Object other$userId = other.getUserId();
    if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
      return false;
    final Object this$photoId = this.getPhotoId();
    final Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Favorite;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Favorite(id="
        + this.getId()
        + ", userId="
        + this.getUserId()
        + ", photoId="
        + this.getPhotoId()
        + ", createdAt="
        + this.getCreatedAt()
        + ")";
  }

  public Favorite() {}

  public Favorite(
      final String id, final String userId, final String photoId, final Instant createdAt) {
    this.id = id;
    this.userId = userId;
    this.photoId = photoId;
    this.createdAt = createdAt;
  }
}
