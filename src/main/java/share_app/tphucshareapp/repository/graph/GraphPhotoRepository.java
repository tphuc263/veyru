package share_app.tphucshareapp.repository.graph;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import share_app.tphucshareapp.model.graph.GraphPhoto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Photo graph operations
 */
public interface GraphPhotoRepository extends Neo4jRepository<GraphPhoto, String> {

    Optional<GraphPhoto> findByPhotoId(String photoId);

    @Query("""
        MATCH (u:User {userId: $userId})-[:POSTED_BY]->(p:Photo)
        WHERE p.createdAt > $cutoffTime
        RETURN p ORDER BY p.createdAt DESC
        """)
    List<GraphPhoto> findRecentPhotosByUser(
        @Param("userId") String userId,
        @Param("cutoffTime") Instant cutoffTime
    );

    @Query("""
        MATCH (u:User)<-[:POSTED_BY]-(p:Photo)
        WHERE u.userId IN $userIds AND p.createdAt > $cutoffTime
        WITH p, u
        ORDER BY p.createdAt DESC
        RETURN p
        """)
    List<GraphPhoto> findRecentPhotosByUsers(
        @Param("userIds") List<String> userIds,
        @Param("cutoffTime") Instant cutoffTime
    );

    @Query("""
        MATCH (u:User {userId: $userId})-[:FOLLOWS]->(f:User)<-[:POSTED_BY]-(p:Photo)
        WHERE p.createdAt > $cutoffTime
        WITH p, f, u
        OPTIONAL MATCH (u2:User)<-[:LIKED]-(p)
        WITH p, f, COUNT(u2) as likeCount
        OPTIONAL MATCH (u3:User)<-[:COMMENTED]-(p)
        WITH p, f, likeCount, COUNT(u3) as commentCount
        RETURN p, likeCount, commentCount
        ORDER BY (likeCount * 2 + commentCount * 5) DESC
        """)
    List<GraphPhoto> findFeedByEngagement(
        @Param("userId") String userId,
        @Param("cutoffTime") Instant cutoffTime
    );

    @Query("""
        MATCH (p:Photo)<-[:LIKED]-(u:User {userId: $userId})
        RETURN p
        """)
    List<GraphPhoto> findLikedPhotos(@Param("userId") String userId);

    @Query("""
        MATCH (p:Photo {photoId: $photoId})-[:POSTED_BY]->(u:User)
        WITH p, u
        OPTIONAL MATCH (similar:User)<-[:POSTED_BY]-(p2:Photo)<-[:LIKED]-(u)
        WITH p, u, COLLECT(DISTINCT p2) as userLikedPhotos
        RETURN userLikedPhotos
        """)
    List<GraphPhoto> findSimilarByUserLikes(@Param("photoId") String photoId);

    @Query("""
        MATCH (p:Photo {photoId: $photoId})-[:POSTED_BY]->(u:User)
        WITH p, u
        MATCH (other:Photo)-[:POSTED_BY]->(u)
        WHERE other.photoId <> $photoId
        RETURN other
        ORDER BY other.createdAt DESC
        LIMIT $limit
        """)
    List<GraphPhoto> findMoreFromSameUser(
        @Param("photoId") String photoId,
        @Param("limit") int limit
    );

    @Query("MATCH (p:Photo) WHERE p.photoId = $photoId DETACH DELETE p")
    void deleteByPhotoId(@Param("photoId") String photoId);

    @Query("MATCH (p:Photo) RETURN COUNT(p)")
    long countAllPhotos();

    @Query("""
        MATCH (u:User {userId: $userId})-[:FOLLOWS]->(f:User)<-[:POSTED_BY]-(p:Photo)
        WHERE p.createdAt > $cutoffTime
        WITH p, f, u
        ORDER BY p.createdAt DESC
        RETURN p
        SKIP $skip LIMIT $limit
        """)
    List<GraphPhoto> findFeedPaginated(
        @Param("userId") String userId,
        @Param("cutoffTime") Instant cutoffTime,
        @Param("skip") int skip,
        @Param("limit") int limit
    );
}

