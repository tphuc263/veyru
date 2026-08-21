package com.veyru.application.messaging;

import com.veyru.application.messaging.MessagingException.Reason;
import com.veyru.application.port.out.ConversationStore;
import com.veyru.application.port.out.MessageNotifier;
import com.veyru.application.port.out.MessageStore;
import com.veyru.application.port.out.MessagingUserLookup;
import com.veyru.domain.model.Message;
import com.veyru.domain.model.Conversation;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class ConversationService {
  private final ConversationStore conversationStore;
  private final MessageStore messageStore;
  private final MessagingUserLookup userLookup;
  private final MessageNotifier notifier;
  private final Clock clock;

  public ConversationService(
      ConversationStore conversationStore,
      MessageStore messageStore,
      MessagingUserLookup userLookup,
      MessageNotifier notifier,
      Clock clock) {
    this.conversationStore = conversationStore;
    this.messageStore = messageStore;
    this.userLookup = userLookup;
    this.notifier = notifier;
    this.clock = clock;
  }

  public List<ConversationResult> getConversations(String userId) {
    List<ConversationResult> results = new ArrayList<>();
    for (Conversation conversation : conversationStore.findByParticipantId(userId)) {
      String otherUserId = conversation.otherParticipant(userId);
      userLookup
          .findById(otherUserId)
          .ifPresent(
              user ->
                  results.add(
                      new ConversationResult(
                          conversation.id(),
                          user.id(),
                          user.username(),
                          user.imageUrl(),
                          conversation.lastMessageText(),
                          conversation.lastMessageSenderId(),
                          conversation.lastMessageAt(),
                          messageStore.countUnread(conversation.id(), userId))));
    }
    return results;
  }

  public PageResult<MessageResult> getMessages(
      String conversationId, String userId, int page, int size) {
    Conversation conversation =
        conversationStore
            .findById(conversationId)
            .orElseThrow(() -> new MessagingException(Reason.RESOURCE_NOT_FOUND));
    if (!conversation.hasParticipant(userId)) {
      throw new MessagingException(Reason.ACCESS_DENIED);
    }
    PageResult<Message> messages =
        messageStore.findByConversationId(conversationId, page, size);
    return new PageResult<>(
        messages.items().stream().map(MessageResult::from).toList(),
        messages.page(),
        messages.size(),
        messages.totalElements(),
        messages.totalPages());
  }

  public void markMessagesAsRead(String conversationId, String userId) {
    if (messageStore.markUnreadAsRead(conversationId, userId) == 0) {
      return;
    }
    conversationStore
        .findById(conversationId)
        .ifPresent(
            conversation ->
                conversation.participantIds().stream()
                    .filter(id -> !id.equals(userId))
                    .forEach(id -> notifier.messagesRead(id, conversationId, userId)));
  }

  public long getUnreadCount(String userId) {
    return messageStore.countUnread(userId);
  }

  public synchronized Conversation getOrCreateConversation(
      String firstUserId, String secondUserId) {
    return conversationStore
        .findBetween(firstUserId, secondUserId)
        .orElseGet(
            () -> {
              Instant now = clock.instant();
              return conversationStore.save(Conversation.between(firstUserId, secondUserId, now));
            });
  }
}
