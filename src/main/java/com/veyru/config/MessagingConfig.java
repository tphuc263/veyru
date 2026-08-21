package com.veyru.config;

import com.veyru.application.messaging.ConversationService;
import com.veyru.application.messaging.SendMessageUseCase;
import com.veyru.application.port.out.ConversationStore;
import com.veyru.application.port.out.MessageIdempotency;
import com.veyru.application.port.out.MessageNotifier;
import com.veyru.application.port.out.MessageStore;
import com.veyru.application.port.out.MessagingUserLookup;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
  @Bean
  public Clock messagingClock() {
    return Clock.systemUTC();
  }

  @Bean
  public ConversationService conversationService(
      ConversationStore conversationStore,
      MessageStore messageStore,
      MessagingUserLookup userLookup,
      MessageNotifier notifier,
      Clock messagingClock) {
    return new ConversationService(
        conversationStore, messageStore, userLookup, notifier, messagingClock);
  }

  @Bean
  public SendMessageUseCase sendMessageUseCase(
      MessageStore messageStore,
      ConversationStore conversationStore,
      MessagingUserLookup userLookup,
      MessageIdempotency idempotency,
      MessageNotifier notifier,
      ConversationService conversations,
      Clock messagingClock) {
    return new SendMessageUseCase(
        messageStore,
        conversationStore,
        userLookup,
        idempotency,
        notifier,
        conversations,
        messagingClock);
  }
}
