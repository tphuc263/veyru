package share_app.tphucshareapp.dto.response.share;

import lombok.Data;

import java.time.Instant;

@Data
public class ShareResponse {
    private String id;
    private String photoId;
    private String userId;
    private String username;
    private String userImageUrl;
    private String caption;
    private Instant createdAt;
}
