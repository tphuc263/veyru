package com.veyru.application.messaging;

import com.veyru.domain.model.Message;
import java.time.Instant;

public record MessageResult(
    String id,
    String conversationId,
    String senderId,
    String receiverId,
    String text,
    boolean read,
    Instant createdAt) {

  public static MessageResult from(Message message) {
    return new MessageResult(
        message.id(),
        message.conversationId(),
        message.senderId(),
        message.receiverId(),
        message.text(),
        message.read(),
        message.createdAt());
  }
}
