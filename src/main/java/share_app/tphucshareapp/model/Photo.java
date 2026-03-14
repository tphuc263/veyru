package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "photos")
public class Photo {

    @Id
    private String id;
    private String imageUrl;
    private String caption;
    private Instant createdAt;

    private List<String> tags;

    private EmbeddedUser user;

    private long likeCount;
    private long commentCount;
    private long shareCount;

    private List<EmbeddedUserTag> userTags;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddedUser {
        private String userId;
        private String username;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddedUserTag {
        private String taggedUserId;
        private String taggedByUserId;
        private String username;
        private Double positionX;
        private Double positionY;
        private Instant createdAt;
    }
}
