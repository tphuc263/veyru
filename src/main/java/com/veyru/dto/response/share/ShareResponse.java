package com.veyru.dto.response.share;

import java.time.Instant;

public class ShareResponse {
  private String id;
  private String photoId;
  private String userId;
  private String username;
  private String userImageUrl;
  private String caption;
  private Instant createdAt;

  public ShareResponse() {}

  public String getId() {
    return this.id;
  }

  public String getPhotoId() {
    return this.photoId;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getUsername() {
    return this.username;
  }

  public String getUserImageUrl() {
    return this.userImageUrl;
  }

  public String getCaption() {
    return this.caption;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setPhotoId(final String photoId) {
    this.photoId = photoId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setUserImageUrl(final String userImageUrl) {
    this.userImageUrl = userImageUrl;
  }

  public void setCaption(final String caption) {
    this.caption = caption;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof ShareResponse)) return false;
    final ShareResponse other = (ShareResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$photoId = this.getPhotoId();
    final java.lang.Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final java.lang.Object this$userId = this.getUserId();
    final java.lang.Object other$userId = other.getUserId();
    if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
      return false;
    final java.lang.Object this$username = this.getUsername();
    final java.lang.Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final java.lang.Object this$userImageUrl = this.getUserImageUrl();
    final java.lang.Object other$userImageUrl = other.getUserImageUrl();
    if (this$userImageUrl == null
        ? other$userImageUrl != null
        : !this$userImageUrl.equals(other$userImageUrl)) return false;
    final java.lang.Object this$caption = this.getCaption();
    final java.lang.Object other$caption = other.getCaption();
    if (this$caption == null ? other$caption != null : !this$caption.equals(other$caption))
      return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof ShareResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final java.lang.Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final java.lang.Object $caption = this.getCaption();
    result = result * PRIME + ($caption == null ? 43 : $caption.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "ShareResponse(id="
        + this.getId()
        + ", photoId="
        + this.getPhotoId()
        + ", userId="
        + this.getUserId()
        + ", username="
        + this.getUsername()
        + ", userImageUrl="
        + this.getUserImageUrl()
        + ", caption="
        + this.getCaption()
        + ", createdAt="
        + this.getCreatedAt()
        + ")";
  }
}
