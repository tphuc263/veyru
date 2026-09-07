package com.veyru.adapter.out.mongo;

import com.veyru.domain.enums.NotificationType;
import com.veyru.domain.model.Notification;
import com.veyru.domain.model.UserSnapshot;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Mongo representation of a notification aggregate. */
@Document(collection = "notifications")
final class NotificationDocument {
  @Id private String id;
  private String recipientId;
  private String actorId;
  private NotificationType type;
  private String photoId;
  private String commentId;
  private String message;
  private boolean read;
  private Instant createdAt;
  private ActorDocument actor;
  private String thumbnailUrl;

  static NotificationDocument fromDomain(Notification notification) {
    NotificationDocument document = new NotificationDocument();
    document.id = notification.id();
    document.recipientId = notification.recipientId();
    document.actorId = notification.actorId();
    document.type = notification.type();
    document.photoId = notification.photoId();
    document.commentId = notification.commentId();
    document.message = notification.message();
    document.read = notification.read();
    document.createdAt = notification.createdAt();
    document.actor = ActorDocument.fromDomain(notification.actor());
    document.thumbnailUrl = notification.thumbnailUrl();
    return document;
  }

  Notification toDomain() {
    return Notification.restore(
        id,
        recipientId,
        type,
        photoId,
        commentId,
        message,
        read,
        createdAt,
        actor.toDomain(actorId),
        thumbnailUrl);
  }

  static final class ActorDocument {
    private String username;

    static ActorDocument fromDomain(UserSnapshot actor) {
      ActorDocument document = new ActorDocument();
      document.username = actor.username();
      return document;
    }

    UserSnapshot toDomain(String actorId) {
      return new UserSnapshot(actorId, username);
    }
  }
}
