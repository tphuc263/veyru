package com.veyru.dto.response.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
  private String id;
  private String conversationId;
  private String senderId;
  private String receiverId;
  private String text;
  private boolean read;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant createdAt;
}
