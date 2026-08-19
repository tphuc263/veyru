package com.veyru.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.veyru.enums.NotificationType;

import java.io.Serializable;
import java.time.Instant;

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
