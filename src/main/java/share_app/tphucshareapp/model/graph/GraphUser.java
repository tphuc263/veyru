package share_app.tphucshareapp.model.graph;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Neo4j Node representing a User in the social graph
 */
@Node("User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphUser {

    @Id
    @GeneratedValue
    private String id;

    @Property("userId")
    private String userId;

    @Property("username")
    private String username;

    @Property("imageUrl")
    private String imageUrl;

    @Property("followerCount")
    private Long followerCount;

    @Property("photoCount")
    private Long photoCount;

    @Property("bio")
    private String bio;

    public GraphUser(String userId, String username, String imageUrl) {
        this.userId = userId;
        this.username = username;
        this.imageUrl = imageUrl;
        this.followerCount = 0L;
        this.photoCount = 0L;
    }
}
