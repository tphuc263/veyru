package com.veyru.controller.websocket;

import com.veyru.dto.request.message.SendMessageRequest;
import com.veyru.dto.websocket.WsError;
import com.veyru.dto.websocket.WsEventEnvelope;
import com.veyru.exceptions.ApiException;
import com.veyru.exceptions.ErrorCode;
import com.veyru.service.message.MessageService;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketMessageController {
  private static final Logger log = LoggerFactory.getLogger(WebSocketMessageController.class);
  private final MessageService messageService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/chat.send")
  public void sendMessage(
      @Payload WsEventEnvelope<SendMessageRequest> envelope, Principal principal) {
    if (principal == null) {
      log.warn("Unauthenticated user tried to send message");
      return;
    }
    String senderId = principal.getName();
    SendMessageRequest request = envelope.getPayload();
    if (request == null || request.getReceiverId() == null || request.getText() == null) {
      sendError(
          senderId,
          envelope.getClientMessageId(),
          ErrorCode.VALIDATION_FAILED,
          "Message receiver and text are required.");
      return;
    }
    try {
      messageService.sendMessage(
          senderId, request.getReceiverId(), request.getText(), envelope.getClientMessageId());
      log.info("Message sent successfully from {} to {}", senderId, request.getReceiverId());
    } catch (ApiException e) {
      sendError(senderId, envelope.getClientMessageId(), e.code(), e.getMessage());
    } catch (Exception e) {
      log.error("Error processing websocket message from {}", senderId, e);
      sendError(
          senderId,
          envelope.getClientMessageId(),
          ErrorCode.INTERNAL_ERROR,
          ErrorCode.INTERNAL_ERROR.detail());
    }
  }

  private void sendError(String userId, String clientMessageId, ErrorCode code, String detail) {
    messagingTemplate.convertAndSendToUser(
        userId,
        "/queue/messages",
        WsEventEnvelope.<WsError>builder()
            .type("ERROR")
            .clientMessageId(clientMessageId)
            .timestamp(java.time.Instant.now())
            .payload(new WsError(code.name(), detail))
            .build());
  }

  public WebSocketMessageController(
      final MessageService messageService, final SimpMessagingTemplate messagingTemplate) {
    this.messageService = messageService;
    this.messagingTemplate = messagingTemplate;
  }
}
