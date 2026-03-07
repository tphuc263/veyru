package share_app.tphucshareapp.dto.response.comment;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentResponse {
    private String id;
    private String photoId;
    private String userId;
    private String username;
    private String userImageUrl;
    private String text;
    private Instant createdAt;
    
    // Nested comments support
    private String parentCommentId;
    private long likeCount = 0;
    private long replyCount = 0;
    private boolean isLikedByCurrentUser = false;
    
    // Replies (nested comments) - only loaded for top-level comments
    private List<CommentResponse> replies = new ArrayList<>();
    
    // Mentioned users
    private List<MentionedUser> mentionedUsers = new ArrayList<>();
    
    @Data
    public static class MentionedUser {
        private String userId;
        private String username;
    }
}
