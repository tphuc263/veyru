package com.veyru.domain.model;

import com.veyru.domain.enums.NotificationType;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
public class Notification {
  @Id private String id;
  @Indexed private String recipientId; // User who receives the notification
  private String actorId; // User who triggered the notification
  private NotificationType type;
  // Reference IDs based on notification type
  private String photoId;
  private String commentId;
  private String message;
  private boolean read;
  private Instant createdAt;
  // Embedded actor info for quick display
  private EmbeddedActor actor;
  // Thumbnail for the related content (photo thumbnail, etc.)
  private String thumbnailUrl;

  public static class EmbeddedActor {
    private String username;

    public static class EmbeddedActorBuilder {
      private String username;

      EmbeddedActorBuilder() {}

      /**
       * @return {@code this}.
       */
      public Notification.EmbeddedActor.EmbeddedActorBuilder username(final String username) {
        this.username = username;
        return this;
      }

      public Notification.EmbeddedActor build() {
        return new Notification.EmbeddedActor(this.username);
      }

      @Override
      public String toString() {
        return "Notification.EmbeddedActor.EmbeddedActorBuilder(username=" + this.username + ")";
      }
    }

    public static Notification.EmbeddedActor.EmbeddedActorBuilder builder() {
      return new Notification.EmbeddedActor.EmbeddedActorBuilder();
    }

    public String getUsername() {
      return this.username;
    }

    public void setUsername(final String username) {
      this.username = username;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof Notification.EmbeddedActor)) return false;
      final Notification.EmbeddedActor other = (Notification.EmbeddedActor) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$username = this.getUsername();
      final Object other$username = other.getUsername();
      if (this$username == null ? other$username != null : !this$username.equals(other$username))
        return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof Notification.EmbeddedActor;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $username = this.getUsername();
      result = result * PRIME + ($username == null ? 43 : $username.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "Notification.EmbeddedActor(username=" + this.getUsername() + ")";
    }

    public EmbeddedActor() {}

    public EmbeddedActor(final String username) {
      this.username = username;
    }
  }

  public static class NotificationBuilder {
    private String id;
    private String recipientId;
    private String actorId;
    private NotificationType type;
    private String photoId;
    private String commentId;
    private String message;
    private boolean read;
    private Instant createdAt;
    private EmbeddedActor actor;
    private String thumbnailUrl;

    NotificationBuilder() {}

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder id(final String id) {
      this.id = id;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder recipientId(final String recipientId) {
      this.recipientId = recipientId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder actorId(final String actorId) {
      this.actorId = actorId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder type(final NotificationType type) {
      this.type = type;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder photoId(final String photoId) {
      this.photoId = photoId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder commentId(final String commentId) {
      this.commentId = commentId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder message(final String message) {
      this.message = message;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder read(final boolean read) {
      this.read = read;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder createdAt(final Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder actor(final EmbeddedActor actor) {
      this.actor = actor;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public Notification.NotificationBuilder thumbnailUrl(final String thumbnailUrl) {
      this.thumbnailUrl = thumbnailUrl;
      return this;
    }

    public Notification build() {
      return new Notification(
          this.id,
          this.recipientId,
          this.actorId,
          this.type,
          this.photoId,
          this.commentId,
          this.message,
          this.read,
          this.createdAt,
          this.actor,
          this.thumbnailUrl);
    }

    @Override
    public String toString() {
      return "Notification.NotificationBuilder(id="
          + this.id
          + ", recipientId="
          + this.recipientId
          + ", actorId="
          + this.actorId
          + ", type="
          + this.type
          + ", photoId="
          + this.photoId
          + ", commentId="
          + this.commentId
          + ", message="
          + this.message
          + ", read="
          + this.read
          + ", createdAt="
          + this.createdAt
          + ", actor="
          + this.actor
          + ", thumbnailUrl="
          + this.thumbnailUrl
          + ")";
    }
  }

  public static Notification.NotificationBuilder builder() {
    return new Notification.NotificationBuilder();
  }

  public String getId() {
    return this.id;
  }

  public String getRecipientId() {
    return this.recipientId;
  }

  public String getActorId() {
    return this.actorId;
  }

  public NotificationType getType() {
    return this.type;
  }

  public String getPhotoId() {
    return this.photoId;
  }

  public String getCommentId() {
    return this.commentId;
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

  public EmbeddedActor getActor() {
    return this.actor;
  }

  public String getThumbnailUrl() {
    return this.thumbnailUrl;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setRecipientId(final String recipientId) {
    this.recipientId = recipientId;
  }

  public void setActorId(final String actorId) {
    this.actorId = actorId;
  }

  public void setType(final NotificationType type) {
    this.type = type;
  }

  public void setPhotoId(final String photoId) {
    this.photoId = photoId;
  }

  public void setCommentId(final String commentId) {
    this.commentId = commentId;
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

  public void setActor(final EmbeddedActor actor) {
    this.actor = actor;
  }

  public void setThumbnailUrl(final String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Notification)) return false;
    final Notification other = (Notification) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.isRead() != other.isRead()) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$recipientId = this.getRecipientId();
    final Object other$recipientId = other.getRecipientId();
    if (this$recipientId == null
        ? other$recipientId != null
        : !this$recipientId.equals(other$recipientId)) return false;
    final Object this$actorId = this.getActorId();
    final Object other$actorId = other.getActorId();
    if (this$actorId == null ? other$actorId != null : !this$actorId.equals(other$actorId))
      return false;
    final Object this$type = this.getType();
    final Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final Object this$photoId = this.getPhotoId();
    final Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final Object this$commentId = this.getCommentId();
    final Object other$commentId = other.getCommentId();
    if (this$commentId == null ? other$commentId != null : !this$commentId.equals(other$commentId))
      return false;
    final Object this$message = this.getMessage();
    final Object other$message = other.getMessage();
    if (this$message == null ? other$message != null : !this$message.equals(other$message))
      return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final Object this$actor = this.getActor();
    final Object other$actor = other.getActor();
    if (this$actor == null ? other$actor != null : !this$actor.equals(other$actor)) return false;
    final Object this$thumbnailUrl = this.getThumbnailUrl();
    final Object other$thumbnailUrl = other.getThumbnailUrl();
    if (this$thumbnailUrl == null
        ? other$thumbnailUrl != null
        : !this$thumbnailUrl.equals(other$thumbnailUrl)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Notification;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isRead() ? 79 : 97);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $recipientId = this.getRecipientId();
    result = result * PRIME + ($recipientId == null ? 43 : $recipientId.hashCode());
    final Object $actorId = this.getActorId();
    result = result * PRIME + ($actorId == null ? 43 : $actorId.hashCode());
    final Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final Object $commentId = this.getCommentId();
    result = result * PRIME + ($commentId == null ? 43 : $commentId.hashCode());
    final Object $message = this.getMessage();
    result = result * PRIME + ($message == null ? 43 : $message.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $actor = this.getActor();
    result = result * PRIME + ($actor == null ? 43 : $actor.hashCode());
    final Object $thumbnailUrl = this.getThumbnailUrl();
    result = result * PRIME + ($thumbnailUrl == null ? 43 : $thumbnailUrl.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Notification(id="
        + this.getId()
        + ", recipientId="
        + this.getRecipientId()
        + ", actorId="
        + this.getActorId()
        + ", type="
        + this.getType()
        + ", photoId="
        + this.getPhotoId()
        + ", commentId="
        + this.getCommentId()
        + ", message="
        + this.getMessage()
        + ", read="
        + this.isRead()
        + ", createdAt="
        + this.getCreatedAt()
        + ", actor="
        + this.getActor()
        + ", thumbnailUrl="
        + this.getThumbnailUrl()
        + ")";
  }

  public Notification() {}

  public Notification(
      final String id,
      final String recipientId,
      final String actorId,
      final NotificationType type,
      final String photoId,
      final String commentId,
      final String message,
      final boolean read,
      final Instant createdAt,
      final EmbeddedActor actor,
      final String thumbnailUrl) {
    this.id = id;
    this.recipientId = recipientId;
    this.actorId = actorId;
    this.type = type;
    this.photoId = photoId;
    this.commentId = commentId;
    this.message = message;
    this.read = read;
    this.createdAt = createdAt;
    this.actor = actor;
    this.thumbnailUrl = thumbnailUrl;
  }
}
