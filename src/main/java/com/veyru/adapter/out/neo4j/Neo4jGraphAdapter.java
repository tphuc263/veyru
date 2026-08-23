package com.veyru.adapter.out.neo4j;

import com.veyru.application.discovery.GraphFeedItem;
import com.veyru.application.port.out.GraphFeedQuery;
import com.veyru.application.port.out.GraphProjection;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Neo4j Graph Service Handles graph operations including: - Sync data from MongoDB to Neo4j -
 * Dijkstra-based feed ranking - Graph traversal queries
 */
@Service
public class Neo4jGraphAdapter implements GraphProjection, GraphFeedQuery {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(Neo4jGraphAdapter.class);
  private final Driver neo4jDriver;
  private final Clock clock;
  // Weight factors for Dijkstra algorithm
  private static final double FOLLOW_WEIGHT = 0.3;
  private static final double ENGAGEMENT_WEIGHT = 0.4;
  private static final double RECENCY_WEIGHT = 0.2;
  private static final double CONTENT_QUALITY_WEIGHT = 0.1;

  /** Create or update a user node in Neo4j */
  public void upsertUser(
      String userId,
      String username,
      String imageUrl,
      long followerCount,
      long photoCount,
      String bio) {
    String cypher =
        """
      MERGE (u:User {userId: $userId})
      SET u.username = $username,
          u.imageUrl = $imageUrl,
          u.followerCount = $followerCount,
          u.photoCount = $photoCount,
          u.bio = $bio
      RETURN u
      """;
    try (Session session = neo4jDriver.session()) {
      session.run(
          cypher,
          Values.parameters(
              "userId",
              userId,
              "username",
              username,
              "imageUrl",
              imageUrl != null ? imageUrl : "",
              "followerCount",
              followerCount,
              "photoCount",
              photoCount,
              "bio",
              bio != null ? bio : ""));
      log.debug("Upserted user in Neo4j: {}", userId);
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Create follow relationship between users */
  public void createFollowRelationship(String followerId, String followingId) {
    String cypher =
        """
      MATCH (follower:User {userId: $followerId})
      MATCH (following:User {userId: $followingId})
      MERGE (follower)-[:FOLLOWS {followedAt: timestamp(), weight: 1.0}]->(following)
      RETURN follower, following
      """;
    try (Session session = neo4jDriver.session()) {
      session.run(cypher, Values.parameters("followerId", followerId, "followingId", followingId));
      log.debug("Created follow relationship: {} -> {}", followerId, followingId);
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Remove follow relationship */
  public void removeFollowRelationship(String followerId, String followingId) {
    String cypher =
        """
      MATCH (follower:User {userId: $followerId})-[r:FOLLOWS]->(following:User {userId: $followingId})
      DELETE r
      """;
    try (Session session = neo4jDriver.session()) {
      session.run(cypher, Values.parameters("followerId", followerId, "followingId", followingId));
      log.debug("Removed follow relationship: {} -> {}", followerId, followingId);
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Create or update a photo node in Neo4j */
  public void upsertPhoto(
      String photoId,
      String userId,
      String username,
      String imageUrl,
      String caption,
      List<String> tags,
      long likeCount,
      long commentCount,
      long shareCount,
      Instant createdAt) {
    String cypher =
        """
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
      session.run(
          cypher,
          Values.parameters(
              "photoId",
              photoId,
              "userId",
              userId,
              "username",
              username,
              "imageUrl",
              imageUrl,
              "caption",
              caption != null ? caption : "",
              "tags",
              tags != null ? String.join(",", tags) : "",
              "likeCount",
              likeCount,
              "commentCount",
              commentCount,
              "shareCount",
              shareCount,
              "createdAt",
              createdAt.toEpochMilli()));
      log.debug("Upserted photo in Neo4j: {}", photoId);
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Create LIKED relationship between user and photo */
  public void createLikeRelationship(String userId, String photoId) {
    String cypher =
        """
      MATCH (u:User {userId: $userId})
      MATCH (p:Photo {photoId: $photoId})
      MERGE (u)-[:LIKED {likedAt: timestamp(), weight: 1.0}]->(p)
      WITH p
      SET p.likeCount = p.likeCount + 1
      RETURN p
      """;
    try (Session session = neo4jDriver.session()) {
      session.run(cypher, Values.parameters("userId", userId, "photoId", photoId));
      log.debug("Created like relationship: {} -> {}", userId, photoId);
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Remove LIKED relationship */
  public void removeLikeRelationship(String userId, String photoId) {
    String cypher =
        """
      MATCH (u:User {userId: $userId})-[r:LIKED]->(p:Photo {photoId: $photoId})
      DELETE r
      WITH p
      SET p.likeCount = MAX(0, p.likeCount - 1)
      RETURN p
      """;
    try (Session session = neo4jDriver.session()) {
      session.run(cypher, Values.parameters("userId", userId, "photoId", photoId));
      log.debug("Removed like relationship: {} -> {}", userId, photoId);
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Create COMMENTED relationship between user and photo */
  public void createCommentRelationship(String userId, String photoId) {
    String cypher =
        """
      MATCH (u:User {userId: $userId})
      MATCH (p:Photo {photoId: $photoId})
      MERGE (u)-[:COMMENTED {commentedAt: timestamp(), weight: 1.0}]->(p)
      WITH p
      SET p.commentCount = p.commentCount + 1
      RETURN p
      """;
    try (Session session = neo4jDriver.session()) {
      session.run(cypher, Values.parameters("userId", userId, "photoId", photoId));
    } catch (RuntimeException e) {
      throw e;
    }
  }

  // ==================== DIJKSTRA-BASED FEED RANKING ====================
  /**
   * Get personalized feed using Dijkstra-based graph traversal This finds the "shortest path" from
   * current user to photos through: 1. Direct follow connections 2. Engagement patterns (likes,
   * comments) 3. Content similarity
   */
  private List<FeedNode> loadFeedWithDijkstra(String userId, int limit) {
    log.info("Computing Dijkstra-based feed for user: {}", userId);
    // Get candidate photos with their weights
    String cypher =
        """
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
           CASE WHEN photo.caption IS NOT NULL AND photo.caption <> \'\' THEN 10.0 ELSE 0.0 END +
           CASE WHEN photo.tags IS NOT NULL AND photo.tags <> \'\' THEN 5.0 ELSE 0.0 END as qualityScore

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
      Result result =
          session.run(
              cypher,
              Values.parameters(
                  "userId",
                  userId,
                  "cutoffTime",
                  clock.instant().minusSeconds(30L * 24 * 60 * 60).toEpochMilli(),
                  "FOLLOW_WEIGHT",
                  FOLLOW_WEIGHT,
                  "ENGAGEMENT_WEIGHT",
                  ENGAGEMENT_WEIGHT,
                  "RECENCY_WEIGHT",
                  RECENCY_WEIGHT,
                  "CONTENT_QUALITY_WEIGHT",
                  CONTENT_QUALITY_WEIGHT,
                  "limit",
                  limit));
      List<FeedNode> feedNodes = new ArrayList<>();
      while (result.hasNext()) {
        Record record = result.next();
        feedNodes.add(
            new FeedNode(
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
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /**
   * Get feed using weighted shortest path (Dijkstra variant) Considers multiple edge types with
   * different weights
   */
  private List<FeedNode> loadWeightedPathFeed(String userId, int limit, int daysBack) {
    String cypher =
        """
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
               SPLIT(photo.tags, \',\'),
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
      Instant cutoffTime = clock.instant().minusSeconds((long) daysBack * 24 * 60 * 60);
      Result result =
          session.run(
              cypher,
              Values.parameters(
                  "userId", userId, "cutoffTime", cutoffTime.toEpochMilli(), "limit", limit));
      List<FeedNode> feedNodes = new ArrayList<>();
      while (result.hasNext()) {
        Record record = result.next();
        feedNodes.add(
            new FeedNode(
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
    } catch (RuntimeException e) {
      throw e;
    }
  }

  // ==================== HELPER METHODS ====================
  /**
   * Get suggested users based on graph analysis Finds users who are popular among people you follow
   */
  public List<String> getSuggestedUsersFromGraph(String userId, int limit) {
    String cypher =
        """
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
      Result result = session.run(cypher, Values.parameters("userId", userId, "limit", limit));
      List<String> suggestions = new ArrayList<>();
      while (result.hasNext()) {
        suggestions.add(result.next().get("userId").asString());
      }
      return suggestions;
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Delete user and all relationships from graph */
  public void deleteUserFromGraph(String userId) {
    String cypher =
        """
      MATCH (u:User {userId: $userId})
      DETACH DELETE u
      """;
    try (Session session = neo4jDriver.session()) {
      session.run(cypher, Values.parameters("userId", userId));
      log.info("Deleted user from Neo4j graph: {}", userId);
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Delete photo from graph */
  public void deletePhotoFromGraph(String photoId) {
    String cypher =
        """
      MATCH (p:Photo {photoId: $photoId})
      DETACH DELETE p
      """;
    try (Session session = neo4jDriver.session()) {
      session.run(cypher, Values.parameters("photoId", photoId));
      log.info("Deleted photo from Neo4j graph: {}", photoId);
    } catch (RuntimeException e) {
      throw e;
    }
  }

  /** Get graph statistics */
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
    } catch (RuntimeException e) {
      throw e;
    }
    return stats;
  }

  @Override
  public List<GraphFeedItem> getFeedWithDijkstra(String userId, int limit) {
    return loadFeedWithDijkstra(userId, limit).stream()
        .map(node -> new GraphFeedItem(node.getPhotoId(), node.getRelevanceScore()))
        .toList();
  }

  @Override
  public List<GraphFeedItem> getWeightedPathFeed(String userId, int limit, int daysBack) {
    return loadWeightedPathFeed(userId, limit, daysBack).stream()
        .map(node -> new GraphFeedItem(node.getPhotoId(), node.getRelevanceScore()))
        .toList();
  }

  /** Helper class for feed results */
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

    public String getPhotoId() {
      return this.photoId;
    }

    public String getImageUrl() {
      return this.imageUrl;
    }

    public String getCaption() {
      return this.caption;
    }

    public String getTags() {
      return this.tags;
    }

    public String getUserId() {
      return this.userId;
    }

    public String getUsername() {
      return this.username;
    }

    public long getLikeCount() {
      return this.likeCount;
    }

    public long getCommentCount() {
      return this.commentCount;
    }

    public long getShareCount() {
      return this.shareCount;
    }

    public Instant getCreatedAt() {
      return this.createdAt;
    }

    public String getAuthorName() {
      return this.authorName;
    }

    public String getAuthorImageUrl() {
      return this.authorImageUrl;
    }

    public double getRelevanceScore() {
      return this.relevanceScore;
    }

    public void setPhotoId(final String photoId) {
      this.photoId = photoId;
    }

    public void setImageUrl(final String imageUrl) {
      this.imageUrl = imageUrl;
    }

    public void setCaption(final String caption) {
      this.caption = caption;
    }

    public void setTags(final String tags) {
      this.tags = tags;
    }

    public void setUserId(final String userId) {
      this.userId = userId;
    }

    public void setUsername(final String username) {
      this.username = username;
    }

    public void setLikeCount(final long likeCount) {
      this.likeCount = likeCount;
    }

    public void setCommentCount(final long commentCount) {
      this.commentCount = commentCount;
    }

    public void setShareCount(final long shareCount) {
      this.shareCount = shareCount;
    }

    public void setCreatedAt(final Instant createdAt) {
      this.createdAt = createdAt;
    }

    public void setAuthorName(final String authorName) {
      this.authorName = authorName;
    }

    public void setAuthorImageUrl(final String authorImageUrl) {
      this.authorImageUrl = authorImageUrl;
    }

    public void setRelevanceScore(final double relevanceScore) {
      this.relevanceScore = relevanceScore;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof Neo4jGraphAdapter.FeedNode)) return false;
      final Neo4jGraphAdapter.FeedNode other = (Neo4jGraphAdapter.FeedNode) o;
      if (!other.canEqual((Object) this)) return false;
      if (this.getLikeCount() != other.getLikeCount()) return false;
      if (this.getCommentCount() != other.getCommentCount()) return false;
      if (this.getShareCount() != other.getShareCount()) return false;
      if (Double.compare(this.getRelevanceScore(), other.getRelevanceScore()) != 0) return false;
      final Object this$photoId = this.getPhotoId();
      final Object other$photoId = other.getPhotoId();
      if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
        return false;
      final Object this$imageUrl = this.getImageUrl();
      final Object other$imageUrl = other.getImageUrl();
      if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
        return false;
      final Object this$caption = this.getCaption();
      final Object other$caption = other.getCaption();
      if (this$caption == null ? other$caption != null : !this$caption.equals(other$caption))
        return false;
      final Object this$tags = this.getTags();
      final Object other$tags = other.getTags();
      if (this$tags == null ? other$tags != null : !this$tags.equals(other$tags)) return false;
      final Object this$userId = this.getUserId();
      final Object other$userId = other.getUserId();
      if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
        return false;
      final Object this$username = this.getUsername();
      final Object other$username = other.getUsername();
      if (this$username == null ? other$username != null : !this$username.equals(other$username))
        return false;
      final Object this$createdAt = this.getCreatedAt();
      final Object other$createdAt = other.getCreatedAt();
      if (this$createdAt == null
          ? other$createdAt != null
          : !this$createdAt.equals(other$createdAt)) return false;
      final Object this$authorName = this.getAuthorName();
      final Object other$authorName = other.getAuthorName();
      if (this$authorName == null
          ? other$authorName != null
          : !this$authorName.equals(other$authorName)) return false;
      final Object this$authorImageUrl = this.getAuthorImageUrl();
      final Object other$authorImageUrl = other.getAuthorImageUrl();
      if (this$authorImageUrl == null
          ? other$authorImageUrl != null
          : !this$authorImageUrl.equals(other$authorImageUrl)) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof Neo4jGraphAdapter.FeedNode;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final long $likeCount = this.getLikeCount();
      result = result * PRIME + (int) ($likeCount >>> 32 ^ $likeCount);
      final long $commentCount = this.getCommentCount();
      result = result * PRIME + (int) ($commentCount >>> 32 ^ $commentCount);
      final long $shareCount = this.getShareCount();
      result = result * PRIME + (int) ($shareCount >>> 32 ^ $shareCount);
      final long $relevanceScore = Double.doubleToLongBits(this.getRelevanceScore());
      result = result * PRIME + (int) ($relevanceScore >>> 32 ^ $relevanceScore);
      final Object $photoId = this.getPhotoId();
      result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
      final Object $imageUrl = this.getImageUrl();
      result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
      final Object $caption = this.getCaption();
      result = result * PRIME + ($caption == null ? 43 : $caption.hashCode());
      final Object $tags = this.getTags();
      result = result * PRIME + ($tags == null ? 43 : $tags.hashCode());
      final Object $userId = this.getUserId();
      result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
      final Object $username = this.getUsername();
      result = result * PRIME + ($username == null ? 43 : $username.hashCode());
      final Object $createdAt = this.getCreatedAt();
      result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
      final Object $authorName = this.getAuthorName();
      result = result * PRIME + ($authorName == null ? 43 : $authorName.hashCode());
      final Object $authorImageUrl = this.getAuthorImageUrl();
      result = result * PRIME + ($authorImageUrl == null ? 43 : $authorImageUrl.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "Neo4jGraphAdapter.FeedNode(photoId="
          + this.getPhotoId()
          + ", imageUrl="
          + this.getImageUrl()
          + ", caption="
          + this.getCaption()
          + ", tags="
          + this.getTags()
          + ", userId="
          + this.getUserId()
          + ", username="
          + this.getUsername()
          + ", likeCount="
          + this.getLikeCount()
          + ", commentCount="
          + this.getCommentCount()
          + ", shareCount="
          + this.getShareCount()
          + ", createdAt="
          + this.getCreatedAt()
          + ", authorName="
          + this.getAuthorName()
          + ", authorImageUrl="
          + this.getAuthorImageUrl()
          + ", relevanceScore="
          + this.getRelevanceScore()
          + ")";
    }

    public FeedNode(
        final String photoId,
        final String imageUrl,
        final String caption,
        final String tags,
        final String userId,
        final String username,
        final long likeCount,
        final long commentCount,
        final long shareCount,
        final Instant createdAt,
        final String authorName,
        final String authorImageUrl,
        final double relevanceScore) {
      this.photoId = photoId;
      this.imageUrl = imageUrl;
      this.caption = caption;
      this.tags = tags;
      this.userId = userId;
      this.username = username;
      this.likeCount = likeCount;
      this.commentCount = commentCount;
      this.shareCount = shareCount;
      this.createdAt = createdAt;
      this.authorName = authorName;
      this.authorImageUrl = authorImageUrl;
      this.relevanceScore = relevanceScore;
    }
  }

  public Neo4jGraphAdapter(final Driver neo4jDriver, final Clock clock) {
    this.neo4jDriver = neo4jDriver;
    this.clock = clock;
  }
}
