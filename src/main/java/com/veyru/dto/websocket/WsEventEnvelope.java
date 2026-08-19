package com.veyru.dto.websocket;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsEventEnvelope<T> {
  private String type;
  private String clientMessageId;
  private Instant timestamp;
  private T payload;
}
