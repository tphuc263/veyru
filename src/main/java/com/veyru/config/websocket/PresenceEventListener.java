package com.veyru.config.websocket;

import com.veyru.dto.websocket.WsEventEnvelope;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceEventListener {
  private static final Logger log = LoggerFactory.getLogger(PresenceEventListener.class);
  private final SimpMessagingTemplate messagingTemplate;

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    if (headerAccessor.getUser() != null) {
      String userId = headerAccessor.getUser().getName();
      log.info("User connected: {}", userId);
      WsEventEnvelope<String> envelope =
          WsEventEnvelope.<String>builder()
              .type("USER_ONLINE")
              .clientMessageId(UUID.randomUUID().toString())
              .timestamp(Instant.now())
              .payload(userId)
              .build();
      messagingTemplate.convertAndSend("/topic/presence", envelope);
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    if (headerAccessor.getUser() != null) {
      String userId = headerAccessor.getUser().getName();
      log.info("User disconnected: {}", userId);
      WsEventEnvelope<String> envelope =
          WsEventEnvelope.<String>builder()
              .type("USER_OFFLINE")
              .clientMessageId(UUID.randomUUID().toString())
              .timestamp(Instant.now())
              .payload(userId)
              .build();
      messagingTemplate.convertAndSend("/topic/presence", envelope);
    }
  }

  public PresenceEventListener(final SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }
}
