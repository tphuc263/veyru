package com.veyru.application.event;

import com.veyru.domain.enums.NotificationType;
import java.time.Instant;

public record NotificationEvent(
    String recipientId,
    String actorId,
    String actorUsername,
    NotificationType type,
    String photoId,
    String commentId,
    String message,
    String thumbnailUrl,
    Instant createdAt) {}
