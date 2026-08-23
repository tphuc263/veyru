package com.veyru.application.result.notification;

import com.veyru.domain.enums.NotificationType;
import java.time.Instant;

public final class NotificationResult {
  private final String id;
  private final NotificationType type;
  private final String message;
  private final boolean read;
  private final Instant createdAt;
  // Actor information
  private final String actorId;
  private final String actorUsername;
  private final String actorImageUrl;
  // Reference IDs
  private final String photoId;
  private final String commentId;
  // Thumbnail
  private final String thumbnailUrl;

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
    public NotificationResult.NotificationResponseBuilder id(final String id) {
      this.id = id;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder type(final NotificationType type) {
      this.type = type;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder message(final String message) {
      this.message = message;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder read(final boolean read) {
      this.read = read;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder createdAt(final Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder actorId(final String actorId) {
      this.actorId = actorId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder actorUsername(
        final String actorUsername) {
      this.actorUsername = actorUsername;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder actorImageUrl(
        final String actorImageUrl) {
      this.actorImageUrl = actorImageUrl;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder photoId(final String photoId) {
      this.photoId = photoId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder commentId(final String commentId) {
      this.commentId = commentId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public NotificationResult.NotificationResponseBuilder thumbnailUrl(
        final String thumbnailUrl) {
      this.thumbnailUrl = thumbnailUrl;
      return this;
    }

    public NotificationResult build() {
      return new NotificationResult(
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

    @Override
    public String toString() {
      return "NotificationResult.NotificationResponseBuilder(id="
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

  public static NotificationResult.NotificationResponseBuilder builder() {
    return new NotificationResult.NotificationResponseBuilder();
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

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof NotificationResult)) return false;
    final NotificationResult other = (NotificationResult) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.isRead() != other.isRead()) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$type = this.getType();
    final Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final Object this$message = this.getMessage();
    final Object other$message = other.getMessage();
    if (this$message == null ? other$message != null : !this$message.equals(other$message))
      return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final Object this$actorId = this.getActorId();
    final Object other$actorId = other.getActorId();
    if (this$actorId == null ? other$actorId != null : !this$actorId.equals(other$actorId))
      return false;
    final Object this$actorUsername = this.getActorUsername();
    final Object other$actorUsername = other.getActorUsername();
    if (this$actorUsername == null
        ? other$actorUsername != null
        : !this$actorUsername.equals(other$actorUsername)) return false;
    final Object this$actorImageUrl = this.getActorImageUrl();
    final Object other$actorImageUrl = other.getActorImageUrl();
    if (this$actorImageUrl == null
        ? other$actorImageUrl != null
        : !this$actorImageUrl.equals(other$actorImageUrl)) return false;
    final Object this$photoId = this.getPhotoId();
    final Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final Object this$commentId = this.getCommentId();
    final Object other$commentId = other.getCommentId();
    if (this$commentId == null ? other$commentId != null : !this$commentId.equals(other$commentId))
      return false;
    final Object this$thumbnailUrl = this.getThumbnailUrl();
    final Object other$thumbnailUrl = other.getThumbnailUrl();
    if (this$thumbnailUrl == null
        ? other$thumbnailUrl != null
        : !this$thumbnailUrl.equals(other$thumbnailUrl)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof NotificationResult;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isRead() ? 79 : 97);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final Object $message = this.getMessage();
    result = result * PRIME + ($message == null ? 43 : $message.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $actorId = this.getActorId();
    result = result * PRIME + ($actorId == null ? 43 : $actorId.hashCode());
    final Object $actorUsername = this.getActorUsername();
    result = result * PRIME + ($actorUsername == null ? 43 : $actorUsername.hashCode());
    final Object $actorImageUrl = this.getActorImageUrl();
    result = result * PRIME + ($actorImageUrl == null ? 43 : $actorImageUrl.hashCode());
    final Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final Object $commentId = this.getCommentId();
    result = result * PRIME + ($commentId == null ? 43 : $commentId.hashCode());
    final Object $thumbnailUrl = this.getThumbnailUrl();
    result = result * PRIME + ($thumbnailUrl == null ? 43 : $thumbnailUrl.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "NotificationResult(id="
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

  public NotificationResult(
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
