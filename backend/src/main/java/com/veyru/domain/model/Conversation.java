package com.veyru.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Immutable value representing a two-party conversation summary. Participants are distinct and
 * last-message fields form one complete state when present.
 */
public record Conversation(
    String id,
    List<String> participantIds,
    String lastMessageText,
    String lastMessageSenderId,
    Instant lastMessageAt,
    Instant createdAt,
    Instant updatedAt) {
  public Conversation {
    if (id != null) {
      id = DomainRules.required(id, "conversation.id.invalid", "Conversation ID is invalid");
    }
    participantIds = participantIds == null ? List.of() : List.copyOf(participantIds);
    DomainRules.require(
        participantIds.size() == 2
            && participantIds.stream().allMatch(value -> value != null && !value.isBlank())
            && !participantIds.get(0).equals(participantIds.get(1)),
        "conversation.participants.invalid",
        "A conversation requires two distinct participants");
    DomainRules.require(
        createdAt != null && updatedAt != null,
        "conversation.time.required",
        "Conversation timestamps are required");
    boolean hasLastMessage =
        lastMessageText != null || lastMessageSenderId != null || lastMessageAt != null;
    DomainRules.require(
        !hasLastMessage
            || (lastMessageText != null
                && !lastMessageText.isBlank()
                && lastMessageSenderId != null
                && participantIds.contains(lastMessageSenderId)
                && lastMessageAt != null),
        "conversation.last-message.incomplete",
        "Last-message state is incomplete");
    if (lastMessageText != null) {
      lastMessageText = validateMessageText(lastMessageText);
    }
  }

  public static Conversation between(String firstUserId, String secondUserId, Instant now) {
    return new Conversation(null, List.of(firstUserId, secondUserId), null, null, null, now, now);
  }

  public boolean hasParticipant(String userId) {
    return participantIds.contains(userId);
  }

  public String otherParticipant(String userId) {
    DomainRules.require(
        hasParticipant(userId),
        "conversation.participant.missing",
        "User is not a conversation participant");
    return participantIds.get(0).equals(userId) ? participantIds.get(1) : participantIds.get(0);
  }

  public Conversation withRecordedMessage(String text, String senderId, Instant now) {
    DomainRules.require(
        hasParticipant(senderId),
        "conversation.sender.invalid",
        "Message sender is not a conversation participant");
    return new Conversation(id, participantIds, text, senderId, now, createdAt, now);
  }

  private static String validateMessageText(String text) {
    String normalized =
        DomainRules.required(text, "message.text.required", "Message text is required");
    DomainRules.require(
        normalized.length() <= 4_000,
        "message.text.too-long",
        "Message text cannot exceed 4000 characters");
    return normalized;
  }
}
