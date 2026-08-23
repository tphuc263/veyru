package com.veyru.adapter.in.dto.response.message;

import com.veyru.application.messaging.ConversationResult;
import java.time.Instant;

public record ConversationResponse(
    String id,
    String participantId,
    String participantUsername,
    String participantImageUrl,
    String lastMessageText,
    String lastMessageSenderId,
    Instant lastMessageAt,
    long unreadCount) {
  public static ConversationResponse from(ConversationResult value) {
    return new ConversationResponse(
        value.id(),
        value.participantId(),
        value.participantUsername(),
        value.participantImageUrl(),
        value.lastMessageText(),
        value.lastMessageSenderId(),
        value.lastMessageAt(),
        value.unreadCount());
  }
}
