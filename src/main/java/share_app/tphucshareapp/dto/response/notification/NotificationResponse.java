package share_app.tphucshareapp.dto.response.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import share_app.tphucshareapp.enums.NotificationType;

import java.time.Instant;

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
