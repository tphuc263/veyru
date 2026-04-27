package share_app.tphucshareapp.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import share_app.tphucshareapp.dto.websocket.WsEventEnvelope;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PresenceEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() != null) {
            String userId = headerAccessor.getUser().getName();
            log.info("User connected: {}", userId);
            
            WsEventEnvelope<String> envelope = WsEventEnvelope.<String>builder()
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
            
            WsEventEnvelope<String> envelope = WsEventEnvelope.<String>builder()
                    .type("USER_OFFLINE")
                    .clientMessageId(UUID.randomUUID().toString())
                    .timestamp(Instant.now())
                    .payload(userId)
                    .build();
                    
            messagingTemplate.convertAndSend("/topic/presence", envelope);
        }
    }
}
