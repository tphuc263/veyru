package com.veyru.domain.model;

import java.time.Instant;

/**
 * Immutable message value with distinct sender and receiver. Conversation membership remains an
 * application-level invariant because it requires the conversation aggregate.
 */
public record Message(
    String id,
    String conversationId,
    String senderId,
    String receiverId,
    String text,
    boolean read,
    Instant createdAt) {
  public Message {
    if (id != null) id = DomainRules.required(id, "message.id.invalid", "Message ID is invalid");
    conversationId =
        DomainRules.required(
            conversationId, "message.conversation.required", "Conversation is required");
    senderId = DomainRules.required(senderId, "message.sender.required", "Sender is required");
    receiverId =
        DomainRules.required(receiverId, "message.receiver.required", "Receiver is required");
    DomainRules.require(
        !senderId.equals(receiverId),
        "message.self.invalid",
        "Sender and receiver must be different");
    text = DomainRules.required(text, "message.text.required", "Message text is required");
    DomainRules.require(
        text.length() <= 4_000,
        "message.text.too-long",
        "Message text cannot exceed 4000 characters");
    DomainRules.require(createdAt != null, "message.time.required", "Message time is required");
  }

  public static Message create(
      String conversationId, String senderId, String receiverId, String text, Instant createdAt) {
    return new Message(null, conversationId, senderId, receiverId, text, false, createdAt);
  }

  public Message markedRead() {
    return read
        ? this
        : new Message(id, conversationId, senderId, receiverId, text, true, createdAt);
  }
}
