package share_app.tphucshareapp.service.graph;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Neo4j Graph Service
 * Handles graph operations including:
 * - Sync data from MongoDB to Neo4j
 * - Dijkstra-based feed ranking
 * - Graph traversal queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Neo4jGraphService {

    private final Driver neo4jDriver;

    // Weight factors for Dijkstra algorithm
    private static final double FOLLOW_WEIGHT = 0.3;
    private static final double ENGAGEMENT_WEIGHT = 0.4;
    private static final double RECENCY_WEIGHT = 0.2;
    private static final double CONTENT_QUALITY_WEIGHT = 0.1;

    /**
     * Create or update a user node in Neo4j
     */
    public void upsertUser(String userId, String username, String imageUrl,
            long followerCount, long photoCount, String bio) {
        String cypher = """
                MERGE (u:User {userId: $userId})
                SET u.username = $username,
                    u.imageUrl = $imageUrl,
                    u.followerCount = $followerCount,
                    u.photoCount = $photoCount,
                    u.bio = $bio
                RETURN u
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher,
                    Values.parameters(
                            "userId", userId,
                            "username", username,
                            "imageUrl", imageUrl != null ? imageUrl : "",
                            "followerCount", followerCount,
                            "photoCount", photoCount,
                            "bio", bio != null ? bio : ""));
            log.debug("Upserted user in Neo4j: {}", userId);
        } catch (Exception e) {
            log.error("Failed to upsert user in Neo4j: {}", e.getMessage());
        }
    }

    /**
     * Create follow relationship between users
     */
    public void createFollowRelationship(String followerId, String followingId) {
        String cypher = """
                MATCH (follower:User {userId: $followerId})
                MATCH (following:User {userId: $followingId})
                MERGE (follower)-[:FOLLOWS {followedAt: timestamp(), weight: 1.0}]->(following)
                RETURN follower, following
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher,
                    Values.parameters("followerId", followerId, "followingId", followingId));
            log.debug("Created follow relationship: {} -> {}", followerId, followingId);
        } catch (Exception e) {
            log.error("Failed to create follow relationship: {}", e.getMessage());
        }
    }

    /**
     * Remove follow relationship
     */
    public void removeFollowRelationship(String followerId, String followingId) {
        String cypher = """
                MATCH (follower:User {userId: $followerId})-[r:FOLLOWS]->(following:User {userId: $followingId})
                DELETE r
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher,
                    Values.parameters("followerId", followerId, "followingId", followingId));
            log.debug("Removed follow relationship: {} -> {}", followerId, followingId);
        } catch (Exception e) {
            log.error("Failed to remove follow relationship: {}", e.getMessage());
        }
    }

    /**
     * Create or update a photo node in Neo4j
     */
    public void upsertPhoto(String photoId, String userId, String username,
            String imageUrl, String caption, List<String> tags,
            long likeCount, long commentCount, long shareCount,
            Instant createdAt) {
        String cypher = """
                MERGE (p:Photo {photoId: $photoId})
                SET p.imageUrl = $imageUrl,
                    p.caption = $caption,
                    p.tags = $tags,
                    p.userId = $userId,
                    p.username = $username,
                    p.likeCount = $likeCount,
                    p.commentCount = $commentCount,
                    p.shareCount = $shareCount,
                    p.createdAt = $createdAt
                WITH p
                MATCH (u:User {userId: $userId})
                MERGE (p)-[:POSTED_BY]->(u)
                RETURN p
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher,
                    Values.parameters(
                            "photoId", photoId,
                            "userId", userId,
                            "username", username,
                            "imageUrl", imageUrl,
                            "caption", caption != null ? caption : "",
                            "tags", tags != null ? String.join(",", tags) : "",
                            "likeCount", likeCount,
                            "commentCount", commentCount,
                            "shareCount", shareCount,
                            "createdAt", createdAt.toEpochMilli()));
            log.debug("Upserted photo in Neo4j: {}", photoId);
        } catch (Exception e) {
            log.error("Failed to upsert photo in Neo4j: {}", e.getMessage());
        }
    }

    /**
     * Create LIKED relationship between user and photo
     */
    public void createLikeRelationship(String userId, String photoId) {
        String cypher = """
                MATCH (u:User {userId: $userId})
                MATCH (p:Photo {photoId: $photoId})
                MERGE (u)-[:LIKED {likedAt: timestamp(), weight: 1.0}]->(p)
                WITH p
                SET p.likeCount = p.likeCount + 1
                RETURN p
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher,
                    Values.parameters("userId", userId, "photoId", photoId));
            log.debug("Created like relationship: {} -> {}", userId, photoId);
        } catch (Exception e) {
            log.error("Failed to create like relationship: {}", e.getMessage());
        }
    }

    /**
     * Remove LIKED relationship
     */
    public void removeLikeRelationship(String userId, String photoId) {
        String cypher = """
                MATCH (u:User {userId: $userId})-[r:LIKED]->(p:Photo {photoId: $photoId})
                DELETE r
                WITH p
                SET p.likeCount = MAX(0, p.likeCount - 1)
                RETURN p
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher,
                    Values.parameters("userId", userId, "photoId", photoId));
            log.debug("Removed like relationship: {} -> {}", userId, photoId);
        } catch (Exception e) {
            log.error("Failed to remove like relationship: {}", e.getMessage());
        }
    }

    /**
     * Create COMMENTED relationship between user and photo
     */
    public void createCommentRelationship(String userId, String photoId) {
        String cypher = """
                MATCH (u:User {userId: $userId})
                MATCH (p:Photo {photoId: $photoId})
                MERGE (u)-[:COMMENTED {commentedAt: timestamp(), weight: 1.0}]->(p)
                WITH p
                SET p.commentCount = p.commentCount + 1
                RETURN p
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher,
                    Values.parameters("userId", userId, "photoId", photoId));
        } catch (Exception e) {
            log.error("Failed to create comment relationship: {}", e.getMessage());
        }
    }

    // ==================== DIJKSTRA-BASED FEED RANKING ====================

    /**
     * Get personalized feed using Dijkstra-based graph traversal
     * This finds the "shortest path" from current user to photos through:
     * 1. Direct follow connections
     * 2. Engagement patterns (likes, comments)
     * 3. Content similarity
     */
    public List<FeedNode> getFeedWithDijkstra(String userId, int limit) {
        log.info("Computing Dijkstra-based feed for user: {}", userId);

        // Get candidate photos with their weights
        String cypher = """
                // Get all photos from followed users
                MATCH (currentUser:User {userId: $userId})-[:FOLLOWS]->(author:User)<-[:POSTED_BY]-(photo:Photo)
                WHERE photo.createdAt > $cutoffTime

                // Calculate weight for each photo
                WITH photo, author, currentUser,
                     // Follow weight: how long have we followed this author?
                     // Shorter follow time = higher weight (more recent interest)
                     COALESCE((currentUser)-[:FOLLOWS]->(author).weight, 1.0) as followWeight,

                     // Engagement weight: total likes + comments on photo
                     (photo.likeCount * 2 + photo.commentCount * 5) as engagementScore,

                     // Recency weight: how recent is the photo?
                     // Newer photos get higher weight
                     CASE
                         WHEN (timestamp() - photo.createdAt) < 86400000 THEN 100.0  // < 24h
                         WHEN (timestamp() - photo.createdAt) < 604800000 THEN 50.0   // < 7 days
                         ELSE 10.0
                     END as recencyWeight,

                     // Content quality: has caption and tags?
                     CASE WHEN photo.caption IS NOT NULL AND photo.caption <> '' THEN 10.0 ELSE 0.0 END +
                     CASE WHEN photo.tags IS NOT NULL AND photo.tags <> '' THEN 5.0 ELSE 0.0 END as qualityScore

                // Calculate final Dijkstra-like score (lower = better path)
                // We invert it so higher score = more relevant
                WITH photo, author,
                     ($FOLLOW_WEIGHT * followWeight +
                      $ENGAGEMENT_WEIGHT * (engagementScore / 100.0) +
                      $RECENCY_WEIGHT * (recencyWeight / 100.0) +
                      $CONTENT_QUALITY_WEIGHT * (qualityScore / 15.0)) as relevanceScore

                RETURN photo.photoId as photoId,
                       photo.imageUrl as imageUrl,
                       photo.caption as caption,
                       photo.tags as tags,
                       photo.userId as userId,
                       photo.username as username,
                       photo.likeCount as likeCount,
                       photo.commentCount as commentCount,
                       photo.shareCount as shareCount,
                       photo.createdAt as createdAt,
                       author.username as authorName,
                       author.imageUrl as authorImageUrl,
                       relevanceScore
                ORDER BY relevanceScore DESC
                LIMIT $limit
                """;

        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher,
                    Values.parameters(
                            "userId", userId,
                            "cutoffTime", Instant.now().minusSeconds(30L * 24 * 60 * 60).toEpochMilli(),
                            "FOLLOW_WEIGHT", FOLLOW_WEIGHT,
                            "ENGAGEMENT_WEIGHT", ENGAGEMENT_WEIGHT,
                            "RECENCY_WEIGHT", RECENCY_WEIGHT,
                            "CONTENT_QUALITY_WEIGHT", CONTENT_QUALITY_WEIGHT,
                            "limit", limit));

            List<FeedNode> feedNodes = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                feedNodes.add(new FeedNode(
                        record.get("photoId").asString(),
                        record.get("imageUrl").asString(),
                        record.get("caption").asString(""),
                        record.get("tags").asString(""),
                        record.get("userId").asString(),
                        record.get("username").asString(),
                        record.get("likeCount").asLong(),
                        record.get("commentCount").asLong(),
                        record.get("shareCount").asLong(),
                        Instant.ofEpochMilli(record.get("createdAt").asLong()),
                        record.get("authorName").asString(),
                        record.get("authorImageUrl").asString(""),
                        record.get("relevanceScore").asDouble()));
            }

            log.info("Found {} candidate photos for user {} using Dijkstra", feedNodes.size(), userId);
            return feedNodes;

        } catch (Exception e) {
            log.error("Failed to compute Dijkstra feed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get feed using weighted shortest path (Dijkstra variant)
     * Considers multiple edge types with different weights
     */
    public List<FeedNode> getWeightedPathFeed(String userId, int limit, int daysBack) {
        String cypher = """
                MATCH (currentUser:User {userId: $userId})

                // Find photos from followed users with path analysis
                OPTIONAL MATCH path = (currentUser)-[:FOLLOWS]->(author:User)<-[:POSTED_BY]-(photo:Photo)
                WHERE photo.createdAt > $cutoffTime

                // Calculate multiple path scores
                WITH photo, author, currentUser,
                     // Path 1: Direct follow (strongest signal)
                     1.0 as directFollowScore,

                     // Path 2: Engagement-based (user likes similar content)
                     COALESCE(SIZE((author)<-[:LIKED]-(:User)<-[:LIKED]-(currentUser)), 0) as mutualEngagement,

                     // Path 3: Content similarity (tags overlap)
                     SIZE(apoc.coll.intersection(
                         SPLIT(photo.tags, ','),
                         COALESCE(currentUser.interestTags, [])
                     )) as tagOverlap,

                     // Path 4: Author popularity (may boost or dampen)
                     CASE WHEN author.followerCount > 1000 THEN 0.8 ELSE 1.0 END as authorPopularity

                // Combine scores into Dijkstra distance (lower = better)
                WITH photo, author,
                     1.0 / (directFollowScore + 0.1) +
                     1.0 / (mutualEngagement + 1) * 0.5 +
                     1.0 / (tagOverlap + 1) * 0.3 +
                     authorPopularity * 0.2 as distance

                RETURN photo.photoId as photoId,
                       photo.imageUrl as imageUrl,
                       photo.caption as caption,
                       photo.tags as tags,
                       photo.userId as userId,
                       photo.username as username,
                       photo.likeCount as likeCount,
                       photo.commentCount as commentCount,
                       photo.shareCount as shareCount,
                       photo.createdAt as createdAt,
                       author.username as authorName,
                       distance
                ORDER BY distance ASC
                LIMIT $limit
                """;

        try (Session session = neo4jDriver.session()) {
            Instant cutoffTime = Instant.now().minusSeconds((long) daysBack * 24 * 60 * 60);

            Result result = session.run(cypher,
                    Values.parameters(
                            "userId", userId,
                            "cutoffTime", cutoffTime.toEpochMilli(),
                            "limit", limit));

            List<FeedNode> feedNodes = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                feedNodes.add(new FeedNode(
                        record.get("photoId").asString(),
                        record.get("imageUrl").asString(),
                        record.get("caption").asString(""),
                        record.get("tags").asString(""),
                        record.get("userId").asString(),
                        record.get("username").asString(),
                        record.get("likeCount").asLong(),
                        record.get("commentCount").asLong(),
                        record.get("shareCount").asLong(),
                        Instant.ofEpochMilli(record.get("createdAt").asLong()),
                        record.get("authorName").asString(""),
                        "",
                        1.0 / record.get("distance").asDouble() // Convert distance to relevance score
                ));
            }

            return feedNodes;

        } catch (Exception e) {
            log.error("Failed to compute weighted path feed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get suggested users based on graph analysis
     * Finds users who are popular among people you follow
     */
    public List<String> getSuggestedUsersFromGraph(String userId, int limit) {
        String cypher = """
                MATCH (currentUser:User {userId: $userId})-[:FOLLOWS]->(f:User)-[:FOLLOWS]->(suggested:User)
                WHERE NOT (currentUser)-[:FOLLOWS]->(suggested)
                  AND suggested.userId <> $userId
                WITH suggested, COUNT(DISTINCT f) as commonFollowers
                RETURN suggested.userId as userId, suggested.username as username,
                       suggested.followerCount as followerCount, commonFollowers
                ORDER BY commonFollowers DESC, followerCount DESC
                LIMIT $limit
                """;

        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher,
                    Values.parameters("userId", userId, "limit", limit));

            List<String> suggestions = new ArrayList<>();
            while (result.hasNext()) {
                suggestions.add(result.next().get("userId").asString());
            }
            return suggestions;
        } catch (Exception e) {
            log.error("Failed to get suggested users from graph: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Delete user and all relationships from graph
     */
    public void deleteUserFromGraph(String userId) {
        String cypher = """
                MATCH (u:User {userId: $userId})
                DETACH DELETE u
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher, Values.parameters("userId", userId));
            log.info("Deleted user from Neo4j graph: {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete user from graph: {}", e.getMessage());
        }
    }

    /**
     * Delete photo from graph
     */
    public void deletePhotoFromGraph(String photoId) {
        String cypher = """
                MATCH (p:Photo {photoId: $photoId})
                DETACH DELETE p
                """;

        try (Session session = neo4jDriver.session()) {
            session.run(cypher, Values.parameters("photoId", photoId));
            log.info("Deleted photo from Neo4j graph: {}", photoId);
        } catch (Exception e) {
            log.error("Failed to delete photo from graph: {}", e.getMessage());
        }
    }

    /**
     * Get graph statistics
     */
    public Map<String, Long> getGraphStats() {
        Map<String, Long> stats = new HashMap<>();

        try (Session session = neo4jDriver.session()) {
            Result userCount = session.run("MATCH (u:User) RETURN COUNT(u) as count");
            if (userCount.hasNext()) {
                stats.put("users", userCount.next().get("count").asLong());
            }

            Result photoCount = session.run("MATCH (p:Photo) RETURN COUNT(p) as count");
            if (photoCount.hasNext()) {
                stats.put("photos", photoCount.next().get("count").asLong());
            }

            Result followCount = session.run("MATCH ()-[r:FOLLOWS]->() RETURN COUNT(r) as count");
            if (followCount.hasNext()) {
                stats.put("follows", followCount.next().get("count").asLong());
            }

            Result likeCount = session.run("MATCH ()-[r:LIKED]->() RETURN COUNT(r) as count");
            if (likeCount.hasNext()) {
                stats.put("likes", likeCount.next().get("count").asLong());
            }
        } catch (Exception e) {
            log.error("Failed to get graph stats: {}", e.getMessage());
        }

        return stats;
    }

    /**
     * Helper class for feed results
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class FeedNode {
        private String photoId;
        private String imageUrl;
        private String caption;
        private String tags;
        private String userId;
        private String username;
        private long likeCount;
        private long commentCount;
        private long shareCount;
        private Instant createdAt;
        private String authorName;
        private String authorImageUrl;
        private double relevanceScore;
    }
}
