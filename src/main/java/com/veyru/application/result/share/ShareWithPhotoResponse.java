package com.veyru.application.result.share;

import java.time.Instant;

public class ShareWithPhotoResponse {
  private String id;
  private String photoId;
  private String userId;
  private String username;
  private String userImageUrl;
  private String caption;
  private Instant createdAt;
  // Thông tin ảnh gốc được share
  private String originalPhotoId;
  private String originalImageUrl;
  private String originalCaption;
  private String originalUsername;
  private String originalUserImageUrl;
  private Instant originalCreatedAt;
  private int originalLikeCount;
  private int originalCommentCount;
  private int originalShareCount;

  public ShareWithPhotoResponse() {}

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

  public String getOriginalPhotoId() {
    return this.originalPhotoId;
  }

  public String getOriginalImageUrl() {
    return this.originalImageUrl;
  }

  public String getOriginalCaption() {
    return this.originalCaption;
  }

  public String getOriginalUsername() {
    return this.originalUsername;
  }

  public String getOriginalUserImageUrl() {
    return this.originalUserImageUrl;
  }

  public Instant getOriginalCreatedAt() {
    return this.originalCreatedAt;
  }

  public int getOriginalLikeCount() {
    return this.originalLikeCount;
  }

  public int getOriginalCommentCount() {
    return this.originalCommentCount;
  }

  public int getOriginalShareCount() {
    return this.originalShareCount;
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

  public void setOriginalPhotoId(final String originalPhotoId) {
    this.originalPhotoId = originalPhotoId;
  }

  public void setOriginalImageUrl(final String originalImageUrl) {
    this.originalImageUrl = originalImageUrl;
  }

  public void setOriginalCaption(final String originalCaption) {
    this.originalCaption = originalCaption;
  }

  public void setOriginalUsername(final String originalUsername) {
    this.originalUsername = originalUsername;
  }

  public void setOriginalUserImageUrl(final String originalUserImageUrl) {
    this.originalUserImageUrl = originalUserImageUrl;
  }

  public void setOriginalCreatedAt(final Instant originalCreatedAt) {
    this.originalCreatedAt = originalCreatedAt;
  }

  public void setOriginalLikeCount(final int originalLikeCount) {
    this.originalLikeCount = originalLikeCount;
  }

  public void setOriginalCommentCount(final int originalCommentCount) {
    this.originalCommentCount = originalCommentCount;
  }

  public void setOriginalShareCount(final int originalShareCount) {
    this.originalShareCount = originalShareCount;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ShareWithPhotoResponse)) return false;
    final ShareWithPhotoResponse other = (ShareWithPhotoResponse) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.getOriginalLikeCount() != other.getOriginalLikeCount()) return false;
    if (this.getOriginalCommentCount() != other.getOriginalCommentCount()) return false;
    if (this.getOriginalShareCount() != other.getOriginalShareCount()) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$photoId = this.getPhotoId();
    final Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final Object this$userId = this.getUserId();
    final Object other$userId = other.getUserId();
    if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
      return false;
    final Object this$username = this.getUsername();
    final Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final Object this$userImageUrl = this.getUserImageUrl();
    final Object other$userImageUrl = other.getUserImageUrl();
    if (this$userImageUrl == null
        ? other$userImageUrl != null
        : !this$userImageUrl.equals(other$userImageUrl)) return false;
    final Object this$caption = this.getCaption();
    final Object other$caption = other.getCaption();
    if (this$caption == null ? other$caption != null : !this$caption.equals(other$caption))
      return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final Object this$originalPhotoId = this.getOriginalPhotoId();
    final Object other$originalPhotoId = other.getOriginalPhotoId();
    if (this$originalPhotoId == null
        ? other$originalPhotoId != null
        : !this$originalPhotoId.equals(other$originalPhotoId)) return false;
    final Object this$originalImageUrl = this.getOriginalImageUrl();
    final Object other$originalImageUrl = other.getOriginalImageUrl();
    if (this$originalImageUrl == null
        ? other$originalImageUrl != null
        : !this$originalImageUrl.equals(other$originalImageUrl)) return false;
    final Object this$originalCaption = this.getOriginalCaption();
    final Object other$originalCaption = other.getOriginalCaption();
    if (this$originalCaption == null
        ? other$originalCaption != null
        : !this$originalCaption.equals(other$originalCaption)) return false;
    final Object this$originalUsername = this.getOriginalUsername();
    final Object other$originalUsername = other.getOriginalUsername();
    if (this$originalUsername == null
        ? other$originalUsername != null
        : !this$originalUsername.equals(other$originalUsername)) return false;
    final Object this$originalUserImageUrl = this.getOriginalUserImageUrl();
    final Object other$originalUserImageUrl = other.getOriginalUserImageUrl();
    if (this$originalUserImageUrl == null
        ? other$originalUserImageUrl != null
        : !this$originalUserImageUrl.equals(other$originalUserImageUrl)) return false;
    final Object this$originalCreatedAt = this.getOriginalCreatedAt();
    final Object other$originalCreatedAt = other.getOriginalCreatedAt();
    if (this$originalCreatedAt == null
        ? other$originalCreatedAt != null
        : !this$originalCreatedAt.equals(other$originalCreatedAt)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ShareWithPhotoResponse;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.getOriginalLikeCount();
    result = result * PRIME + this.getOriginalCommentCount();
    result = result * PRIME + this.getOriginalShareCount();
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final Object $caption = this.getCaption();
    result = result * PRIME + ($caption == null ? 43 : $caption.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $originalPhotoId = this.getOriginalPhotoId();
    result = result * PRIME + ($originalPhotoId == null ? 43 : $originalPhotoId.hashCode());
    final Object $originalImageUrl = this.getOriginalImageUrl();
    result = result * PRIME + ($originalImageUrl == null ? 43 : $originalImageUrl.hashCode());
    final Object $originalCaption = this.getOriginalCaption();
    result = result * PRIME + ($originalCaption == null ? 43 : $originalCaption.hashCode());
    final Object $originalUsername = this.getOriginalUsername();
    result = result * PRIME + ($originalUsername == null ? 43 : $originalUsername.hashCode());
    final Object $originalUserImageUrl = this.getOriginalUserImageUrl();
    result =
        result * PRIME + ($originalUserImageUrl == null ? 43 : $originalUserImageUrl.hashCode());
    final Object $originalCreatedAt = this.getOriginalCreatedAt();
    result = result * PRIME + ($originalCreatedAt == null ? 43 : $originalCreatedAt.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "ShareWithPhotoResponse(id="
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
        + ", originalPhotoId="
        + this.getOriginalPhotoId()
        + ", originalImageUrl="
        + this.getOriginalImageUrl()
        + ", originalCaption="
        + this.getOriginalCaption()
        + ", originalUsername="
        + this.getOriginalUsername()
        + ", originalUserImageUrl="
        + this.getOriginalUserImageUrl()
        + ", originalCreatedAt="
        + this.getOriginalCreatedAt()
        + ", originalLikeCount="
        + this.getOriginalLikeCount()
        + ", originalCommentCount="
        + this.getOriginalCommentCount()
        + ", originalShareCount="
        + this.getOriginalShareCount()
        + ")";
  }
}
