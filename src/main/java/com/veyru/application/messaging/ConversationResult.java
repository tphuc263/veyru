package com.veyru.application.messaging;

import java.time.Instant;

public record ConversationResult(
    String id,
    String participantId,
    String participantUsername,
    String participantImageUrl,
    String lastMessageText,
    String lastMessageSenderId,
    Instant lastMessageAt,
    long unreadCount) {}
