package com.veyru.domain.model;

import java.time.Instant;
import java.util.List;

public record Conversation(
    String id,
    List<String> participantIds,
    String lastMessageText,
    String lastMessageSenderId,
    Instant lastMessageAt,
    Instant createdAt,
    Instant updatedAt) {

  public Conversation {
    participantIds = List.copyOf(participantIds);
  }

  public static Conversation between(String firstUserId, String secondUserId, Instant now) {
    return new Conversation(null, List.of(firstUserId, secondUserId), null, null, null, now, now);
  }

  public boolean hasParticipant(String userId) {
    return participantIds.contains(userId);
  }

  public String otherParticipant(String userId) {
    return participantIds.stream().filter(id -> !id.equals(userId)).findFirst().orElse(userId);
  }

  public Conversation recordLastMessage(String text, String senderId, Instant now) {
    return new Conversation(id, participantIds, text, senderId, now, createdAt, now);
  }
}
