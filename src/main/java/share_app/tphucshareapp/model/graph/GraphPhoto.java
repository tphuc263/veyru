package share_app.tphucshareapp.model.graph;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Neo4j Node representing a Photo/Post in the social graph
 */
@Node("Photo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphPhoto {

    @Id
    @GeneratedValue
    private String id;

    @Property("photoId")
    private String photoId;

    @Property("imageUrl")
    private String imageUrl;

    @Property("caption")
    private String caption;

    @Property("tags")
    private String tags; // Stored as comma-separated string

    @Property("userId")
    private String userId;

    @Property("username")
    private String username;

    @Property("likeCount")
    private Long likeCount;

    @Property("commentCount")
    private Long commentCount;

    @Property("shareCount")
    private Long shareCount;

    @Property("createdAt")
    private Instant createdAt;

    // Relationship to author
    @Relationship(type = "POSTED_BY", direction = Relationship.Direction.INCOMING)
    private GraphUser author;

    // Relationship to users who liked
    @Relationship(type = "LIKED", direction = Relationship.Direction.INCOMING)
    private Set<GraphUser> likedBy = new HashSet<>();

    // Relationship to users who commented
    @Relationship(type = "COMMENTED", direction = Relationship.Direction.INCOMING)
    private Set<GraphUser> commentedBy = new HashSet<>();

    public GraphPhoto(String photoId, String userId, String username, String imageUrl) {
        this.photoId = photoId;
        this.userId = userId;
        this.username = username;
        this.imageUrl = imageUrl;
        this.likeCount = 0L;
        this.commentCount = 0L;
        this.shareCount = 0L;
        this.createdAt = Instant.now();
    }
}
