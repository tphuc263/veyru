package com.veyru.dto.response.post;

import java.time.Instant;

/**
 * Unified post response that can represent either a photo or a shared photo Similar to Instagram's
 * unified feed
 */
public class UnifiedPostResponse {
  private String id;
  private PostType type; // PHOTO or SHARE
  private Instant createdAt;
  // User who posted/shared
  private String userId;
  private String username;
  private String userImageUrl;
  // For PHOTO type - original photo fields
  private String imageUrl;
  private String caption;
  private int likeCount;
  private int commentCount;
  private int shareCount;
  private boolean isLikedByCurrentUser;
  private boolean isSavedByCurrentUser;
  // For SHARE type - share specific fields
  private String shareCaption; // Caption added by the sharer
  // Original photo info (for SHARE type)
  private String originalPhotoId;
  private String originalImageUrl;
  private String originalCaption;
  private String originalUsername;
  private String originalUserImageUrl;
  private Instant originalCreatedAt;
  private int originalLikeCount;
  private int originalCommentCount;
  private int originalShareCount;

  public enum PostType {
    PHOTO,
    SHARE;
  }

  public UnifiedPostResponse() {}

  public String getId() {
    return this.id;
  }

  public PostType getType() {
    return this.type;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
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

  public String getImageUrl() {
    return this.imageUrl;
  }

  public String getCaption() {
    return this.caption;
  }

  public int getLikeCount() {
    return this.likeCount;
  }

  public int getCommentCount() {
    return this.commentCount;
  }

  public int getShareCount() {
    return this.shareCount;
  }

  public boolean isLikedByCurrentUser() {
    return this.isLikedByCurrentUser;
  }

  public boolean isSavedByCurrentUser() {
    return this.isSavedByCurrentUser;
  }

  public String getShareCaption() {
    return this.shareCaption;
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

  public void setType(final PostType type) {
    this.type = type;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
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

  public void setImageUrl(final String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setCaption(final String caption) {
    this.caption = caption;
  }

  public void setLikeCount(final int likeCount) {
    this.likeCount = likeCount;
  }

  public void setCommentCount(final int commentCount) {
    this.commentCount = commentCount;
  }

  public void setShareCount(final int shareCount) {
    this.shareCount = shareCount;
  }

  public void setLikedByCurrentUser(final boolean isLikedByCurrentUser) {
    this.isLikedByCurrentUser = isLikedByCurrentUser;
  }

  public void setSavedByCurrentUser(final boolean isSavedByCurrentUser) {
    this.isSavedByCurrentUser = isSavedByCurrentUser;
  }

  public void setShareCaption(final String shareCaption) {
    this.shareCaption = shareCaption;
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

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof UnifiedPostResponse)) return false;
    final UnifiedPostResponse other = (UnifiedPostResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.getLikeCount() != other.getLikeCount()) return false;
    if (this.getCommentCount() != other.getCommentCount()) return false;
    if (this.getShareCount() != other.getShareCount()) return false;
    if (this.isLikedByCurrentUser() != other.isLikedByCurrentUser()) return false;
    if (this.isSavedByCurrentUser() != other.isSavedByCurrentUser()) return false;
    if (this.getOriginalLikeCount() != other.getOriginalLikeCount()) return false;
    if (this.getOriginalCommentCount() != other.getOriginalCommentCount()) return false;
    if (this.getOriginalShareCount() != other.getOriginalShareCount()) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$type = this.getType();
    final java.lang.Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
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
    final java.lang.Object this$imageUrl = this.getImageUrl();
    final java.lang.Object other$imageUrl = other.getImageUrl();
    if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
      return false;
    final java.lang.Object this$caption = this.getCaption();
    final java.lang.Object other$caption = other.getCaption();
    if (this$caption == null ? other$caption != null : !this$caption.equals(other$caption))
      return false;
    final java.lang.Object this$shareCaption = this.getShareCaption();
    final java.lang.Object other$shareCaption = other.getShareCaption();
    if (this$shareCaption == null
        ? other$shareCaption != null
        : !this$shareCaption.equals(other$shareCaption)) return false;
    final java.lang.Object this$originalPhotoId = this.getOriginalPhotoId();
    final java.lang.Object other$originalPhotoId = other.getOriginalPhotoId();
    if (this$originalPhotoId == null
        ? other$originalPhotoId != null
        : !this$originalPhotoId.equals(other$originalPhotoId)) return false;
    final java.lang.Object this$originalImageUrl = this.getOriginalImageUrl();
    final java.lang.Object other$originalImageUrl = other.getOriginalImageUrl();
    if (this$originalImageUrl == null
        ? other$originalImageUrl != null
        : !this$originalImageUrl.equals(other$originalImageUrl)) return false;
    final java.lang.Object this$originalCaption = this.getOriginalCaption();
    final java.lang.Object other$originalCaption = other.getOriginalCaption();
    if (this$originalCaption == null
        ? other$originalCaption != null
        : !this$originalCaption.equals(other$originalCaption)) return false;
    final java.lang.Object this$originalUsername = this.getOriginalUsername();
    final java.lang.Object other$originalUsername = other.getOriginalUsername();
    if (this$originalUsername == null
        ? other$originalUsername != null
        : !this$originalUsername.equals(other$originalUsername)) return false;
    final java.lang.Object this$originalUserImageUrl = this.getOriginalUserImageUrl();
    final java.lang.Object other$originalUserImageUrl = other.getOriginalUserImageUrl();
    if (this$originalUserImageUrl == null
        ? other$originalUserImageUrl != null
        : !this$originalUserImageUrl.equals(other$originalUserImageUrl)) return false;
    final java.lang.Object this$originalCreatedAt = this.getOriginalCreatedAt();
    final java.lang.Object other$originalCreatedAt = other.getOriginalCreatedAt();
    if (this$originalCreatedAt == null
        ? other$originalCreatedAt != null
        : !this$originalCreatedAt.equals(other$originalCreatedAt)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof UnifiedPostResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.getLikeCount();
    result = result * PRIME + this.getCommentCount();
    result = result * PRIME + this.getShareCount();
    result = result * PRIME + (this.isLikedByCurrentUser() ? 79 : 97);
    result = result * PRIME + (this.isSavedByCurrentUser() ? 79 : 97);
    result = result * PRIME + this.getOriginalLikeCount();
    result = result * PRIME + this.getOriginalCommentCount();
    result = result * PRIME + this.getOriginalShareCount();
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final java.lang.Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final java.lang.Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final java.lang.Object $caption = this.getCaption();
    result = result * PRIME + ($caption == null ? 43 : $caption.hashCode());
    final java.lang.Object $shareCaption = this.getShareCaption();
    result = result * PRIME + ($shareCaption == null ? 43 : $shareCaption.hashCode());
    final java.lang.Object $originalPhotoId = this.getOriginalPhotoId();
    result = result * PRIME + ($originalPhotoId == null ? 43 : $originalPhotoId.hashCode());
    final java.lang.Object $originalImageUrl = this.getOriginalImageUrl();
    result = result * PRIME + ($originalImageUrl == null ? 43 : $originalImageUrl.hashCode());
    final java.lang.Object $originalCaption = this.getOriginalCaption();
    result = result * PRIME + ($originalCaption == null ? 43 : $originalCaption.hashCode());
    final java.lang.Object $originalUsername = this.getOriginalUsername();
    result = result * PRIME + ($originalUsername == null ? 43 : $originalUsername.hashCode());
    final java.lang.Object $originalUserImageUrl = this.getOriginalUserImageUrl();
    result =
        result * PRIME + ($originalUserImageUrl == null ? 43 : $originalUserImageUrl.hashCode());
    final java.lang.Object $originalCreatedAt = this.getOriginalCreatedAt();
    result = result * PRIME + ($originalCreatedAt == null ? 43 : $originalCreatedAt.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "UnifiedPostResponse(id="
        + this.getId()
        + ", type="
        + this.getType()
        + ", createdAt="
        + this.getCreatedAt()
        + ", userId="
        + this.getUserId()
        + ", username="
        + this.getUsername()
        + ", userImageUrl="
        + this.getUserImageUrl()
        + ", imageUrl="
        + this.getImageUrl()
        + ", caption="
        + this.getCaption()
        + ", likeCount="
        + this.getLikeCount()
        + ", commentCount="
        + this.getCommentCount()
        + ", shareCount="
        + this.getShareCount()
        + ", isLikedByCurrentUser="
        + this.isLikedByCurrentUser()
        + ", isSavedByCurrentUser="
        + this.isSavedByCurrentUser()
        + ", shareCaption="
        + this.getShareCaption()
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
