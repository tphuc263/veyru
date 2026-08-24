package com.veyru.adapter.in.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.veyru.adapter.in.dto.SendMessageRequest;
import com.veyru.adapter.in.dto.websocket.WsError;
import com.veyru.adapter.in.dto.websocket.WsEventEnvelope;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.messaging.SendMessageCommand;
import com.veyru.application.messaging.SendMessageUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class WebSocketMessageControllerTest {
  @Test
  void preservesAccessDeniedWebSocketError() {
    SendMessageUseCase sendMessage = mock(SendMessageUseCase.class);
    doThrow(new UseCaseException(UseCaseError.ACCESS_DENIED))
        .when(sendMessage)
        .execute(org.mockito.ArgumentMatchers.any(SendMessageCommand.class));
    SimpMessagingTemplate messages = mock(SimpMessagingTemplate.class);
    WebSocketMessageController controller =
        new WebSocketMessageController(
            sendMessage,
            messages,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    controller.sendMessage(
        WsEventEnvelope.<SendMessageRequest>builder()
            .clientMessageId("client-1")
            .payload(new SendMessageRequest("conversation-1", "recipient-1", "hello"))
            .build(),
        () -> "sender-1");

    ArgumentCaptor<WsEventEnvelope> event = ArgumentCaptor.forClass(WsEventEnvelope.class);
    verify(messages).convertAndSendToUser(eq("sender-1"), eq("/queue/messages"), event.capture());
    WsError error = (WsError) event.getValue().getPayload();
    assertThat(error.code()).isEqualTo("ACCESS_DENIED");
  }
}
