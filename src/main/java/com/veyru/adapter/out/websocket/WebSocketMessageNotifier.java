package com.veyru.adapter.out.websocket;

import com.veyru.adapter.in.dto.websocket.WsEventEnvelope;
import com.veyru.application.messaging.MessageResult;
import com.veyru.application.port.out.MessageNotifier;
import java.time.Clock;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessageNotifier implements MessageNotifier {
  private static final Logger log = LoggerFactory.getLogger(WebSocketMessageNotifier.class);
  private static final String DESTINATION = "/queue/messages";
  private final SimpMessagingTemplate messagingTemplate;
  private final Clock clock;

  public WebSocketMessageNotifier(SimpMessagingTemplate messagingTemplate, Clock clock) {
    this.messagingTemplate = messagingTemplate;
    this.clock = clock;
  }

  @Override
  public void messageSent(
      String senderId, String receiverId, String clientMessageId, MessageResult message) {
    try {
      WsEventEnvelope<MessageResult> envelope =
          WsEventEnvelope.<MessageResult>builder()
              .type("CHAT_MESSAGE")
              .clientMessageId(clientMessageId)
              .timestamp(clock.instant())
              .payload(message)
              .build();
      messagingTemplate.convertAndSendToUser(receiverId, DESTINATION, envelope);
      messagingTemplate.convertAndSendToUser(senderId, DESTINATION, envelope);
    } catch (RuntimeException exception) {
      throw exception;
    }
  }

  @Override
  public void messagesRead(String recipientId, String conversationId, String readBy) {
    try {
      messagingTemplate.convertAndSendToUser(
          recipientId,
          DESTINATION,
          WsEventEnvelope.<Map<String, String>>builder()
              .type("MESSAGES_READ")
              .timestamp(clock.instant())
              .payload(Map.of("conversationId", conversationId, "readBy", readBy))
              .build());
    } catch (RuntimeException exception) {
      throw exception;
    }
  }
}
