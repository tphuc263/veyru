package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import share_app.tphucshareapp.enums.NotificationType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    
    @Indexed
    private String recipientId; // User who receives the notification
    
    private String actorId; // User who triggered the notification
    
    private NotificationType type;
    
    // Reference IDs based on notification type
    private String photoId;
    private String commentId;
    
    private String message;
    
    private boolean read;
    
    private Instant createdAt;
    
    // Embedded actor info for quick display
    private EmbeddedActor actor;
    
    // Thumbnail for the related content (photo thumbnail, etc.)
    private String thumbnailUrl;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmbeddedActor {
        private String username;
        private String userImageUrl;
    }
}
