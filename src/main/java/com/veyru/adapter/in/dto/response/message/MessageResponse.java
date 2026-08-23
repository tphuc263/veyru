package com.veyru.adapter.in.dto.response.message;

import com.veyru.application.messaging.MessageResult;
import java.time.Instant;

public record MessageResponse(
    String id,
    String conversationId,
    String senderId,
    String receiverId,
    String text,
    boolean read,
    Instant createdAt) {
  public static MessageResponse from(MessageResult value) {
    return new MessageResponse(
        value.id(), value.conversationId(), value.senderId(), value.receiverId(), value.text(),
        value.read(), value.createdAt());
  }
}
