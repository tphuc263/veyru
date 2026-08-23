package com.veyru.adapter.out.websocket;

import com.veyru.adapter.in.dto.response.notification.NotificationResponse;
import com.veyru.adapter.in.dto.websocket.WsEventEnvelope;
import com.veyru.application.port.out.NotificationNotifier;
import com.veyru.application.result.notification.NotificationResult;
import java.time.Clock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketNotificationNotifier implements NotificationNotifier {
  private final SimpMessagingTemplate messaging;
  private final Clock clock;

  @Override
  public void send(String userId, NotificationResult notification) {
    messaging.convertAndSendToUser(
        userId,
        "/queue/notifications",
        WsEventEnvelope.builder()
            .type("NOTIFICATION")
            .timestamp(clock.instant())
            .payload(NotificationResponse.from(notification))
            .build());
  }

  public WebSocketNotificationNotifier(SimpMessagingTemplate messaging, Clock clock) {
    this.messaging = messaging;
    this.clock = clock;
  }
}
