package com.veyru.adapter.out.websocket;

import com.veyru.adapter.in.dto.response.message.MessageResponse;
import com.veyru.adapter.in.dto.websocket.WsEventEnvelope;
import com.veyru.application.messaging.MessageResult;
import com.veyru.application.port.out.MessageNotifier;
import java.time.Clock;
import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessageNotifier implements MessageNotifier {
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
    WsEventEnvelope<MessageResponse> envelope =
        WsEventEnvelope.<MessageResponse>builder()
            .type("CHAT_MESSAGE")
            .clientMessageId(clientMessageId)
            .timestamp(clock.instant())
            .payload(MessageResponse.from(message))
            .build();
    messagingTemplate.convertAndSendToUser(receiverId, DESTINATION, envelope);
    messagingTemplate.convertAndSendToUser(senderId, DESTINATION, envelope);
  }

  @Override
  public void messagesRead(String recipientId, String conversationId, String readBy) {
    messagingTemplate.convertAndSendToUser(
        recipientId,
        DESTINATION,
        WsEventEnvelope.<Map<String, String>>builder()
            .type("MESSAGES_READ")
            .timestamp(clock.instant())
            .payload(Map.of("conversationId", conversationId, "readBy", readBy))
            .build());
  }
}
