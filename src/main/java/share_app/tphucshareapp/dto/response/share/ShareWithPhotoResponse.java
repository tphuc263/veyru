package share_app.tphucshareapp.dto.response.share;

import lombok.Data;

import java.time.Instant;

@Data
public class ShareWithPhotoResponse {
    private String id;
    private String photoId;
    private String userId;
    private String username;
    private String userImageUrl;
    private String caption;
    private Instant createdAt;
    
    // Thông tin ảnh gốc được share
    private String originalPhotoId;
    private String originalImageUrl;
    private String originalCaption;
    private String originalUsername;
    private String originalUserImageUrl;
    private Instant originalCreatedAt;
    private int originalLikeCount;
    private int originalCommentCount;
    private int originalShareCount;
}

