package com.veyru.adapter.out.websocket;

import com.veyru.adapter.in.dto.websocket.WsEventEnvelope;
import com.veyru.application.port.out.NotificationNotifier;
import java.time.Instant;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketNotificationNotifier implements NotificationNotifier {
  private final SimpMessagingTemplate messaging;

  @Override
  public void send(String userId, Object notification) {
    messaging.convertAndSendToUser(
        userId,
        "/queue/notifications",
        WsEventEnvelope.builder()
            .type("NOTIFICATION")
            .timestamp(Instant.now())
            .payload(notification)
            .build());
  }

  public WebSocketNotificationNotifier(SimpMessagingTemplate messaging) {
    this.messaging = messaging;
  }
}
