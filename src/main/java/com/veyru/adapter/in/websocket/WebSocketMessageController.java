package com.veyru.adapter.in.websocket;

import com.veyru.adapter.in.dto.SendMessageRequest;
import com.veyru.adapter.in.dto.websocket.WsError;
import com.veyru.adapter.in.dto.websocket.WsEventEnvelope;
import com.veyru.adapter.in.error.ErrorCode;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.messaging.SendMessageCommand;
import com.veyru.application.messaging.SendMessageUseCase;
import java.security.Principal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketMessageController {
  private static final Logger log = LoggerFactory.getLogger(WebSocketMessageController.class);
  private final SendMessageUseCase sendMessage;
  private final SimpMessagingTemplate messagingTemplate;
  private final Clock clock;

  public WebSocketMessageController(
      SendMessageUseCase sendMessage, SimpMessagingTemplate messagingTemplate, Clock clock) {
    this.sendMessage = sendMessage;
    this.messagingTemplate = messagingTemplate;
    this.clock = clock;
  }

  @MessageMapping("/chat.send")
  public void sendMessage(
      @Payload WsEventEnvelope<SendMessageRequest> envelope, Principal principal) {
    if (principal == null) {
      log.warn("Unauthenticated user tried to send message");
      return;
    }

    String senderId = principal.getName();
    SendMessageRequest request = envelope.getPayload();
    if (request == null
        || request.conversationId() == null
        || request.receiverId() == null
        || request.text() == null) {
      sendError(
          senderId,
          envelope.getClientMessageId(),
          ErrorCode.VALIDATION_FAILED,
          "Conversation, receiver and text are required.");
      return;
    }

    try {
      sendMessage.execute(
          new SendMessageCommand(
              request.conversationId(),
              senderId,
              request.receiverId(),
              request.text(),
              envelope.getClientMessageId()));
    } catch (UseCaseException exception) {
      ErrorCode code = ErrorCode.valueOf(exception.code().name());
      sendError(senderId, envelope.getClientMessageId(), code, code.detail());
    } catch (RuntimeException exception) {
      log.error("Error processing websocket message from {}", senderId, exception);
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
            .timestamp(clock.instant())
            .payload(new WsError(code.name(), detail))
            .build());
  }
}
