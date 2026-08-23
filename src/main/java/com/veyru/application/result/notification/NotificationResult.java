package com.veyru.application.result.notification;

import com.veyru.domain.enums.NotificationType;
import java.time.Instant;

public record NotificationResult(
    String id,
    NotificationType type,
    String message,
    boolean read,
    Instant createdAt,
    String actorId,
    String actorUsername,
    String actorImageUrl,
    String photoId,
    String commentId,
    String thumbnailUrl) {
  public String getId() { return id; }
  public NotificationType getType() { return type; }
  public String getMessage() { return message; }
  public boolean isRead() { return read; }
  public Instant getCreatedAt() { return createdAt; }
  public String getActorId() { return actorId; }
  public String getActorUsername() { return actorUsername; }
  public String getActorImageUrl() { return actorImageUrl; }
  public String getPhotoId() { return photoId; }
  public String getCommentId() { return commentId; }
  public String getThumbnailUrl() { return thumbnailUrl; }
}
