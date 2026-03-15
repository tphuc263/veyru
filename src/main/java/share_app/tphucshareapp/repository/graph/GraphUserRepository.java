package share_app.tphucshareapp.repository.graph;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import share_app.tphucshareapp.model.graph.GraphUser;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User graph operations
 */
public interface GraphUserRepository extends Neo4jRepository<GraphUser, String> {

    Optional<GraphUser> findByUserId(String userId);

    @Query("MATCH (u:User {userId: $userId})-[:FOLLOWS]->(following:User) RETURN following")
    List<GraphUser> findFollowing(@Param("userId") String userId);

    @Query("MATCH (u:User {userId: $userId})<-[:FOLLOWS]-(follower:User) RETURN follower")
    List<GraphUser> findFollowers(@Param("userId") String userId);

    @Query("""
        MATCH (u:User {userId: $userId})-[:FOLLOWS]->(f:User)
        WITH f, SIZE((f)-[:LIKED]->(:Photo)<-[:POSTED_BY]-(:User)) as engagement
        RETURN f ORDER BY engagement DESC LIMIT $limit
        """)
    List<GraphUser> findMostEngagedFollowing(@Param("userId") String userId, @Param("limit") int limit);

    @Query("""
        MATCH (u:User {userId: $userId})-[:FOLLOWS]->(f:User)
        WITH f, SIZE((f)-[:POSTED_BY]->(:Photo)) as photoCount
        RETURN f ORDER BY photoCount DESC LIMIT $limit
        """)
    List<GraphUser> findMostActiveFollowing(@Param("userId") String userId, @Param("limit") int limit);

    @Query("MATCH (u:User) RETURN u ORDER BY u.followerCount DESC LIMIT $limit")
    List<GraphUser> findPopularUsers(@Param("limit") int limit);

    @Query("MATCH (u:User {userId: $userId})-[:FOLLOWS]->(f:User) RETURN COUNT(f)")
    long countFollowing(@Param("userId") String userId);

    @Query("MATCH (u:User {userId: $userId})<-[:FOLLOWS]-(f:User) RETURN COUNT(f)")
    long countFollowers(@Param("userId") String userId);

    void deleteByUserId(String userId);
}

