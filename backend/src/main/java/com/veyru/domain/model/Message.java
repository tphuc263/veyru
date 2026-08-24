package com.veyru.domain.model;

import java.time.Instant;

public record Message(
    String id,
    String conversationId,
    String senderId,
    String receiverId,
    String text,
    boolean read,
    Instant createdAt) {

  public static Message create(
      String conversationId, String senderId, String receiverId, String text, Instant createdAt) {
    return new Message(null, conversationId, senderId, receiverId, text, false, createdAt);
  }
}
