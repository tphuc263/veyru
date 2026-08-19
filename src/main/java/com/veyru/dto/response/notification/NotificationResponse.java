package com.veyru.dto.response.notification;

import com.veyru.enums.NotificationType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
  private String id;
  private NotificationType type;
  private String message;
  private boolean read;
  private Instant createdAt;

  // Actor information
  private String actorId;
  private String actorUsername;
  private String actorImageUrl;

  // Reference IDs
  private String photoId;
  private String commentId;

  // Thumbnail
  private String thumbnailUrl;
}
