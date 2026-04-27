package share_app.tphucshareapp.controller.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import share_app.tphucshareapp.dto.request.message.SendMessageRequest;
import share_app.tphucshareapp.dto.websocket.WsEventEnvelope;
import share_app.tphucshareapp.service.message.MessageService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload WsEventEnvelope<SendMessageRequest> envelope, Principal principal) {
        if (principal == null) {
            log.warn("Unauthenticated user tried to send message");
            return;
        }

        String senderId = principal.getName();
        SendMessageRequest request = envelope.getPayload();

        if (request == null || request.getReceiverId() == null || request.getText() == null) {
            log.warn("Invalid message payload");
            return;
        }

        try {
            messageService.sendMessage(
                senderId,
                request.getReceiverId(),
                request.getText(),
                envelope.getClientMessageId()
            );
            log.info("Message sent successfully from {} to {}", senderId, request.getReceiverId());
        } catch (Exception e) {
            log.error("Error processing websocket message from {}", senderId, e);
        }
    }
}
