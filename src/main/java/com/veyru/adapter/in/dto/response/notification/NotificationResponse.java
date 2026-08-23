package com.veyru.adapter.in.dto.response.notification;

import com.veyru.application.result.notification.NotificationResult;
import com.veyru.domain.enums.NotificationType;
import java.time.Instant;

public record NotificationResponse(
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
  public static NotificationResponse from(NotificationResult value) {
    return new NotificationResponse(
        value.getId(),
        value.getType(),
        value.getMessage(),
        value.isRead(),
        value.getCreatedAt(),
        value.getActorId(),
        value.getActorUsername(),
        value.getActorImageUrl(),
        value.getPhotoId(),
        value.getCommentId(),
        value.getThumbnailUrl());
  }
}
