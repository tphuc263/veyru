package com.veyru.dto.response.usertag;

import java.time.Instant;

public class UserTagResponse {
  private String id;
  private String photoId;
  private String taggedUserId;
  private String taggedByUserId;
  private String username;
  private String userImageUrl;
  private Double positionX;
  private Double positionY;
  private Instant createdAt;

  public static class UserTagResponseBuilder {
    private String id;
    private String photoId;
    private String taggedUserId;
    private String taggedByUserId;
    private String username;
    private String userImageUrl;
    private Double positionX;
    private Double positionY;
    private Instant createdAt;

    UserTagResponseBuilder() {}

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder id(final String id) {
      this.id = id;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder photoId(final String photoId) {
      this.photoId = photoId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder taggedUserId(final String taggedUserId) {
      this.taggedUserId = taggedUserId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder taggedByUserId(final String taggedByUserId) {
      this.taggedByUserId = taggedByUserId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder username(final String username) {
      this.username = username;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder userImageUrl(final String userImageUrl) {
      this.userImageUrl = userImageUrl;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder positionX(final Double positionX) {
      this.positionX = positionX;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder positionY(final Double positionY) {
      this.positionY = positionY;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public UserTagResponse.UserTagResponseBuilder createdAt(final Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public UserTagResponse build() {
      return new UserTagResponse(
          this.id,
          this.photoId,
          this.taggedUserId,
          this.taggedByUserId,
          this.username,
          this.userImageUrl,
          this.positionX,
          this.positionY,
          this.createdAt);
    }

    @java.lang.Override
    public java.lang.String toString() {
      return "UserTagResponse.UserTagResponseBuilder(id="
          + this.id
          + ", photoId="
          + this.photoId
          + ", taggedUserId="
          + this.taggedUserId
          + ", taggedByUserId="
          + this.taggedByUserId
          + ", username="
          + this.username
          + ", userImageUrl="
          + this.userImageUrl
          + ", positionX="
          + this.positionX
          + ", positionY="
          + this.positionY
          + ", createdAt="
          + this.createdAt
          + ")";
    }
  }

  public static UserTagResponse.UserTagResponseBuilder builder() {
    return new UserTagResponse.UserTagResponseBuilder();
  }

  public String getId() {
    return this.id;
  }

  public String getPhotoId() {
    return this.photoId;
  }

  public String getTaggedUserId() {
    return this.taggedUserId;
  }

  public String getTaggedByUserId() {
    return this.taggedByUserId;
  }

  public String getUsername() {
    return this.username;
  }

  public String getUserImageUrl() {
    return this.userImageUrl;
  }

  public Double getPositionX() {
    return this.positionX;
  }

  public Double getPositionY() {
    return this.positionY;
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

  public void setTaggedUserId(final String taggedUserId) {
    this.taggedUserId = taggedUserId;
  }

  public void setTaggedByUserId(final String taggedByUserId) {
    this.taggedByUserId = taggedByUserId;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setUserImageUrl(final String userImageUrl) {
    this.userImageUrl = userImageUrl;
  }

  public void setPositionX(final Double positionX) {
    this.positionX = positionX;
  }

  public void setPositionY(final Double positionY) {
    this.positionY = positionY;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof UserTagResponse)) return false;
    final UserTagResponse other = (UserTagResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$positionX = this.getPositionX();
    final java.lang.Object other$positionX = other.getPositionX();
    if (this$positionX == null ? other$positionX != null : !this$positionX.equals(other$positionX))
      return false;
    final java.lang.Object this$positionY = this.getPositionY();
    final java.lang.Object other$positionY = other.getPositionY();
    if (this$positionY == null ? other$positionY != null : !this$positionY.equals(other$positionY))
      return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$photoId = this.getPhotoId();
    final java.lang.Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final java.lang.Object this$taggedUserId = this.getTaggedUserId();
    final java.lang.Object other$taggedUserId = other.getTaggedUserId();
    if (this$taggedUserId == null
        ? other$taggedUserId != null
        : !this$taggedUserId.equals(other$taggedUserId)) return false;
    final java.lang.Object this$taggedByUserId = this.getTaggedByUserId();
    final java.lang.Object other$taggedByUserId = other.getTaggedByUserId();
    if (this$taggedByUserId == null
        ? other$taggedByUserId != null
        : !this$taggedByUserId.equals(other$taggedByUserId)) return false;
    final java.lang.Object this$username = this.getUsername();
    final java.lang.Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final java.lang.Object this$userImageUrl = this.getUserImageUrl();
    final java.lang.Object other$userImageUrl = other.getUserImageUrl();
    if (this$userImageUrl == null
        ? other$userImageUrl != null
        : !this$userImageUrl.equals(other$userImageUrl)) return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof UserTagResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $positionX = this.getPositionX();
    result = result * PRIME + ($positionX == null ? 43 : $positionX.hashCode());
    final java.lang.Object $positionY = this.getPositionY();
    result = result * PRIME + ($positionY == null ? 43 : $positionY.hashCode());
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final java.lang.Object $taggedUserId = this.getTaggedUserId();
    result = result * PRIME + ($taggedUserId == null ? 43 : $taggedUserId.hashCode());
    final java.lang.Object $taggedByUserId = this.getTaggedByUserId();
    result = result * PRIME + ($taggedByUserId == null ? 43 : $taggedByUserId.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "UserTagResponse(id="
        + this.getId()
        + ", photoId="
        + this.getPhotoId()
        + ", taggedUserId="
        + this.getTaggedUserId()
        + ", taggedByUserId="
        + this.getTaggedByUserId()
        + ", username="
        + this.getUsername()
        + ", userImageUrl="
        + this.getUserImageUrl()
        + ", positionX="
        + this.getPositionX()
        + ", positionY="
        + this.getPositionY()
        + ", createdAt="
        + this.getCreatedAt()
        + ")";
  }

  public UserTagResponse() {}

  public UserTagResponse(
      final String id,
      final String photoId,
      final String taggedUserId,
      final String taggedByUserId,
      final String username,
      final String userImageUrl,
      final Double positionX,
      final Double positionY,
      final Instant createdAt) {
    this.id = id;
    this.photoId = photoId;
    this.taggedUserId = taggedUserId;
    this.taggedByUserId = taggedByUserId;
    this.username = username;
    this.userImageUrl = userImageUrl;
    this.positionX = positionX;
    this.positionY = positionY;
    this.createdAt = createdAt;
  }
}
