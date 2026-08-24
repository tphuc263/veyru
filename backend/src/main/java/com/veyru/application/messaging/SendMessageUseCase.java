package com.veyru.application.messaging;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.port.out.ConversationStore;
import com.veyru.application.port.out.MessageIdempotency;
import com.veyru.application.port.out.MessageNotifier;
import com.veyru.application.port.out.MessageStore;
import com.veyru.application.port.out.MessagingUserLookup;
import com.veyru.domain.model.Conversation;
import com.veyru.domain.model.Message;
import java.time.Clock;
import java.time.Instant;

public final class SendMessageUseCase {
  private final MessageStore messageStore;
  private final ConversationStore conversationStore;
  private final MessagingUserLookup userLookup;
  private final MessageIdempotency idempotency;
  private final MessageNotifier notifier;
  private final Clock clock;

  public SendMessageUseCase(
      MessageStore messageStore,
      ConversationStore conversationStore,
      MessagingUserLookup userLookup,
      MessageIdempotency idempotency,
      MessageNotifier notifier,
      Clock clock) {
    this.messageStore = messageStore;
    this.conversationStore = conversationStore;
    this.userLookup = userLookup;
    this.idempotency = idempotency;
    this.notifier = notifier;
    this.clock = clock;
  }

  public MessageResult execute(SendMessageCommand command) {
    requireUser(command.senderId());
    requireUser(command.receiverId());
    Conversation conversation =
        conversationStore
            .findById(command.conversationId())
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    if (!conversation.hasParticipant(command.senderId())
        || !conversation.hasParticipant(command.receiverId())
        || command.senderId().equals(command.receiverId())) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }

    Instant now = clock.instant();
    if (hasClientMessageId(command)
        && !idempotency.claim(command.senderId(), command.clientMessageId())) {
      return new MessageResult(
          null,
          command.conversationId(),
          command.senderId(),
          command.receiverId(),
          command.text(),
          false,
          now);
    }
    Message saved =
        messageStore.save(
            Message.create(
                command.conversationId(),
                command.senderId(),
                command.receiverId(),
                command.text(),
                now));
    conversationStore.save(conversation.recordLastMessage(command.text(), command.senderId(), now));

    MessageResult result = MessageResult.from(saved);
    notifier.messageSent(
        command.senderId(), command.receiverId(), command.clientMessageId(), result);
    return result;
  }

  private void requireUser(String userId) {
    if (userLookup.findById(userId).isEmpty()) {
      throw new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND);
    }
  }

  private boolean hasClientMessageId(SendMessageCommand command) {
    return command.clientMessageId() != null && !command.clientMessageId().isBlank();
  }
}
