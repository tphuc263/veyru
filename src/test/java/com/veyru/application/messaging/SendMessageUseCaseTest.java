package com.veyru.application.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SendMessageUseCaseTest {
  private final MessageStore messages = mock(MessageStore.class);
  private final ConversationStore conversations = mock(ConversationStore.class);
  private final MessagingUserLookup users = mock(MessagingUserLookup.class);
  private final MessageIdempotency idempotency = mock(MessageIdempotency.class);
  private final MessageNotifier notifier = mock(MessageNotifier.class);
  private final Instant now = Instant.parse("2026-08-25T00:00:00Z");
  private final SendMessageUseCase sendMessage =
      new SendMessageUseCase(
          messages, conversations, users, idempotency, notifier, Clock.fixed(now, ZoneOffset.UTC));

  @BeforeEach
  void usersExist() {
    when(users.findById(any()))
        .thenReturn(Optional.of(new MessagingUserLookup.UserSummary("user", "user", null)));
  }

  @Test
  void savesOnlyInsideTheRequestedConversation() {
    var conversation =
        new Conversation(
            "conversation-1", List.of("sender", "receiver"), null, null, null, now, now);
    when(conversations.findById("conversation-1")).thenReturn(Optional.of(conversation));
    when(messages.save(any()))
        .thenAnswer(
            invocation -> {
              Message value = invocation.getArgument(0);
              return new Message(
                  "message-1",
                  value.conversationId(),
                  value.senderId(),
                  value.receiverId(),
                  value.text(),
                  value.read(),
                  value.createdAt());
            });

    sendMessage.execute(
        new SendMessageCommand("conversation-1", "sender", "receiver", "hello", null));

    verify(messages).save(Message.create("conversation-1", "sender", "receiver", "hello", now));
  }

  @Test
  void rejectsSenderOrReceiverOutsideConversation() {
    var conversation =
        new Conversation(
            "conversation-1", List.of("sender", "someone-else"), null, null, null, now, now);
    when(conversations.findById("conversation-1")).thenReturn(Optional.of(conversation));

    assertThatThrownBy(
            () ->
                sendMessage.execute(
                    new SendMessageCommand(
                        "conversation-1", "sender", "receiver", "hello", "client-1")))
        .isInstanceOf(UseCaseException.class);
    verify(idempotency, never()).claim(any(), any());
    verify(messages, never()).save(any());
  }
}
