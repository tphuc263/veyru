package share_app.tphucshareapp.model.graph;

import java.time.Instant;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.TargetNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Relationship: User likes Photo
 * Used for calculating engagement scores
 */
@org.springframework.data.neo4j.core.schema.RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikedRelationship {

    @Id
    @GeneratedValue
    private String id;

    @Property("type")
    private String type = "LIKED";

    @Property("likedAt")
    private Instant likedAt;

    @Property("weight")
    private Double weight; // Base weight for like interaction

    @TargetNode
    private GraphPhoto photo;

    public LikedRelationship(GraphPhoto photo) {
        this.photo = photo;
        this.likedAt = Instant.now();
        this.weight = 1.0;
    }
}
