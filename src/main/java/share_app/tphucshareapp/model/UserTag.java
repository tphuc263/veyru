package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a user being tagged in a photo.
 * Similar to Instagram's feature where you can tag people in photos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_tags")
@CompoundIndex(name = "photo_user_idx", def = "{'photoId': 1, 'taggedUserId': 1}", unique = true)
public class UserTag {
    @Id
    private String id;
    
    @Indexed
    private String photoId;
    
    @Indexed
    private String taggedUserId;  // User who is tagged
    
    private String taggedByUserId; // User who tagged
    
    // Position on the photo (x, y coordinates as percentage 0-100)
    private Double positionX;
    private Double positionY;
    
    private Instant createdAt;
    
    // Embedded user info for quick access
    private EmbeddedUser taggedUser;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddedUser {
        private String username;
        private String userImageUrl;
    }
}
