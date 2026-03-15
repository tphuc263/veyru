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
 * Relationship: User follows User
 * Represents the follow connection between users
 */
@org.springframework.data.neo4j.core.schema.RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRelationship {

    @Id
    @GeneratedValue
    private String id;

    @Property("type")
    private String type = "FOLLOWS";

    @Property("followedAt")
    private Instant followedAt;

    @Property("weight")
    private Double weight; // Weight based on follow duration

    @TargetNode
    private GraphUser targetUser;

    public FollowRelationship(GraphUser targetUser) {
        this.targetUser = targetUser;
        this.followedAt = Instant.now();
        this.weight = 1.0;
    }
}
