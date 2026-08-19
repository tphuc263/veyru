package com.veyru.event;

import com.veyru.enums.NotificationType;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent implements Serializable {
  private static final long serialVersionUID = 1L;

  private String recipientId;
  private String actorId;
  private String actorUsername;
  private NotificationType type;
  private String photoId;
  private String commentId;
  private String message;
  private String thumbnailUrl;
  private Instant createdAt;
}
