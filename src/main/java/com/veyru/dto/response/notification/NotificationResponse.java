package com.veyru.dto.response.notification;

import com.veyru.enums.NotificationType;
import java.time.Instant;

public class NotificationResponse {
  private String id;
  private NotificationType type;
  private String message;
  private boolean read;
  private Instant createdAt;
  // Actor information
  private String actorId;
  private String actorUsername;
  private String actorImageUrl;
  // Reference IDs
  private String photoId;
  private String commentId;
  // Thumbnail
  private String thumbnailUrl;

  public static class NotificationResponseBuilder {
    private String id;
    private NotificationType type;
    private String message;
    private boolean read;
    private Instant createdAt;
    private String actorId;
    private String actorUsername;
    private String actorImageUrl;
    private String photoId;
    private String commentId;
    private String thumbnailUrl;

    NotificationResponseBuilder() {}

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder id(final String id) {
      this.id = id;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder type(final NotificationType type) {
      this.type = type;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder message(final String message) {
      this.message = message;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder read(final boolean read) {
      this.read = read;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder createdAt(final Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder actorId(final String actorId) {
      this.actorId = actorId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder actorUsername(
        final String actorUsername) {
      this.actorUsername = actorUsername;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder actorImageUrl(
        final String actorImageUrl) {
      this.actorImageUrl = actorImageUrl;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder photoId(final String photoId) {
      this.photoId = photoId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder commentId(final String commentId) {
      this.commentId = commentId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResponse.NotificationResponseBuilder thumbnailUrl(
        final String thumbnailUrl) {
      this.thumbnailUrl = thumbnailUrl;
      return this;
    }

    public NotificationResponse build() {
      return new NotificationResponse(
          this.id,
          this.type,
          this.message,
          this.read,
          this.createdAt,
          this.actorId,
          this.actorUsername,
          this.actorImageUrl,
          this.photoId,
          this.commentId,
          this.thumbnailUrl);
    }

    @java.lang.Override
    public java.lang.String toString() {
      return "NotificationResponse.NotificationResponseBuilder(id="
          + this.id
          + ", type="
          + this.type
          + ", message="
          + this.message
          + ", read="
          + this.read
          + ", createdAt="
          + this.createdAt
          + ", actorId="
          + this.actorId
          + ", actorUsername="
          + this.actorUsername
          + ", actorImageUrl="
          + this.actorImageUrl
          + ", photoId="
          + this.photoId
          + ", commentId="
          + this.commentId
          + ", thumbnailUrl="
          + this.thumbnailUrl
          + ")";
    }
  }

  public static NotificationResponse.NotificationResponseBuilder builder() {
    return new NotificationResponse.NotificationResponseBuilder();
  }

  public String getId() {
    return this.id;
  }

  public NotificationType getType() {
    return this.type;
  }

  public String getMessage() {
    return this.message;
  }

  public boolean isRead() {
    return this.read;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public String getActorId() {
    return this.actorId;
  }

  public String getActorUsername() {
    return this.actorUsername;
  }

  public String getActorImageUrl() {
    return this.actorImageUrl;
  }

  public String getPhotoId() {
    return this.photoId;
  }

  public String getCommentId() {
    return this.commentId;
  }

  public String getThumbnailUrl() {
    return this.thumbnailUrl;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setType(final NotificationType type) {
    this.type = type;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setRead(final boolean read) {
    this.read = read;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setActorId(final String actorId) {
    this.actorId = actorId;
  }

  public void setActorUsername(final String actorUsername) {
    this.actorUsername = actorUsername;
  }

  public void setActorImageUrl(final String actorImageUrl) {
    this.actorImageUrl = actorImageUrl;
  }

  public void setPhotoId(final String photoId) {
    this.photoId = photoId;
  }

  public void setCommentId(final String commentId) {
    this.commentId = commentId;
  }

  public void setThumbnailUrl(final String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof NotificationResponse)) return false;
    final NotificationResponse other = (NotificationResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.isRead() != other.isRead()) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$type = this.getType();
    final java.lang.Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final java.lang.Object this$message = this.getMessage();
    final java.lang.Object other$message = other.getMessage();
    if (this$message == null ? other$message != null : !this$message.equals(other$message))
      return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final java.lang.Object this$actorId = this.getActorId();
    final java.lang.Object other$actorId = other.getActorId();
    if (this$actorId == null ? other$actorId != null : !this$actorId.equals(other$actorId))
      return false;
    final java.lang.Object this$actorUsername = this.getActorUsername();
    final java.lang.Object other$actorUsername = other.getActorUsername();
    if (this$actorUsername == null
        ? other$actorUsername != null
        : !this$actorUsername.equals(other$actorUsername)) return false;
    final java.lang.Object this$actorImageUrl = this.getActorImageUrl();
    final java.lang.Object other$actorImageUrl = other.getActorImageUrl();
    if (this$actorImageUrl == null
        ? other$actorImageUrl != null
        : !this$actorImageUrl.equals(other$actorImageUrl)) return false;
    final java.lang.Object this$photoId = this.getPhotoId();
    final java.lang.Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final java.lang.Object this$commentId = this.getCommentId();
    final java.lang.Object other$commentId = other.getCommentId();
    if (this$commentId == null ? other$commentId != null : !this$commentId.equals(other$commentId))
      return false;
    final java.lang.Object this$thumbnailUrl = this.getThumbnailUrl();
    final java.lang.Object other$thumbnailUrl = other.getThumbnailUrl();
    if (this$thumbnailUrl == null
        ? other$thumbnailUrl != null
        : !this$thumbnailUrl.equals(other$thumbnailUrl)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof NotificationResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isRead() ? 79 : 97);
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final java.lang.Object $message = this.getMessage();
    result = result * PRIME + ($message == null ? 43 : $message.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final java.lang.Object $actorId = this.getActorId();
    result = result * PRIME + ($actorId == null ? 43 : $actorId.hashCode());
    final java.lang.Object $actorUsername = this.getActorUsername();
    result = result * PRIME + ($actorUsername == null ? 43 : $actorUsername.hashCode());
    final java.lang.Object $actorImageUrl = this.getActorImageUrl();
    result = result * PRIME + ($actorImageUrl == null ? 43 : $actorImageUrl.hashCode());
    final java.lang.Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final java.lang.Object $commentId = this.getCommentId();
    result = result * PRIME + ($commentId == null ? 43 : $commentId.hashCode());
    final java.lang.Object $thumbnailUrl = this.getThumbnailUrl();
    result = result * PRIME + ($thumbnailUrl == null ? 43 : $thumbnailUrl.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "NotificationResponse(id="
        + this.getId()
        + ", type="
        + this.getType()
        + ", message="
        + this.getMessage()
        + ", read="
        + this.isRead()
        + ", createdAt="
        + this.getCreatedAt()
        + ", actorId="
        + this.getActorId()
        + ", actorUsername="
        + this.getActorUsername()
        + ", actorImageUrl="
        + this.getActorImageUrl()
        + ", photoId="
        + this.getPhotoId()
        + ", commentId="
        + this.getCommentId()
        + ", thumbnailUrl="
        + this.getThumbnailUrl()
        + ")";
  }

  public NotificationResponse() {}

  public NotificationResponse(
      final String id,
      final NotificationType type,
      final String message,
      final boolean read,
      final Instant createdAt,
      final String actorId,
      final String actorUsername,
      final String actorImageUrl,
      final String photoId,
      final String commentId,
      final String thumbnailUrl) {
    this.id = id;
    this.type = type;
    this.message = message;
    this.read = read;
    this.createdAt = createdAt;
    this.actorId = actorId;
    this.actorUsername = actorUsername;
    this.actorImageUrl = actorImageUrl;
    this.photoId = photoId;
    this.commentId = commentId;
    this.thumbnailUrl = thumbnailUrl;
  }
}
