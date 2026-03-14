package share_app.tphucshareapp.dto.response.post;

import lombok.Data;

import java.time.Instant;

/**
 * Unified post response that can represent either a photo or a shared photo
 * Similar to Instagram's unified feed
 */
@Data
public class UnifiedPostResponse {
    private String id;
    private PostType type;  // PHOTO or SHARE
    private Instant createdAt;
    
    // User who posted/shared
    private String userId;
    private String username;
    private String userImageUrl;
    
    // For PHOTO type - original photo fields
    private String imageUrl;
    private String caption;
    private int likeCount;
    private int commentCount;
    private int shareCount;
    private boolean isLikedByCurrentUser;
    private boolean isSavedByCurrentUser;
    
    // For SHARE type - share specific fields
    private String shareCaption;  // Caption added by the sharer
    
    // Original photo info (for SHARE type)
    private String originalPhotoId;
    private String originalImageUrl;
    private String originalCaption;
    private String originalUsername;
    private String originalUserImageUrl;
    private Instant originalCreatedAt;
    private int originalLikeCount;
    private int originalCommentCount;
    private int originalShareCount;
    
    public enum PostType {
        PHOTO,
        SHARE
    }
}

