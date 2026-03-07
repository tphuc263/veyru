package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    
    @Indexed
    private String photoId;
    
    private String userId;
    private String text;
    private Instant createdAt;
    private EmbeddedUser user;
    
    // Nested comments support
    @Indexed
    private String parentCommentId; // null for top-level comments
    
    private long likeCount = 0;
    private long replyCount = 0;
    
    // Mentioned users in comment (when user types @username)
    private List<String> mentionedUserIds = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddedUser {
        private String username;
        private String userImageUrl;
    }
}
