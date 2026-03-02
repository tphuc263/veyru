package share_app.tphucshareapp.dto.response.photo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;
import share_app.tphucshareapp.dto.response.like.LikeResponse;

import java.time.Instant;
import java.util.List;

@Data
public class PhotoDetailResponse {
    private String id;
    private String userId;
    private String username;
    private String userImageUrl;
    private String imageUrl;
    private String caption;
    private Instant createdAt;
    private int likeCount;
    private int commentCount;

    @JsonProperty("isLikedByCurrentUser")
    private boolean isLikedByCurrentUser;

    @JsonProperty("isSavedByCurrentUser")
    private boolean isSavedByCurrentUser;

    private List<LikeResponse> likes;
    private List<CommentResponse> comments;
    private List<String> tags;
}
