package com.veyru.adapter.out.neo4j;

import com.veyru.application.discovery.GraphAffinity;
import com.veyru.application.discovery.GraphFeedItem;
import com.veyru.application.port.out.GraphFeedQuery;
import com.veyru.application.port.out.GraphProjection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Neo4jGraphAdapter implements GraphProjection, GraphFeedQuery {
  private static final Logger log = LoggerFactory.getLogger(Neo4jGraphAdapter.class);
  private final Driver driver;

  @Override
  public void upsertUser(
      String userId,
      String username,
      String imageUrl,
      long followerCount,
      long photoCount,
      String bio) {
    write(
        "upsert user " + userId,
        session ->
            session.run(
                """
                MERGE (u:User {userId: $userId})
                SET u.username = $username,
                    u.imageUrl = $imageUrl,
                    u.followerCount = $followerCount,
                    u.photoCount = $photoCount,
                    u.bio = $bio
                """,
                Values.parameters(
                    "userId",
                    userId,
                    "username",
                    username,
                    "imageUrl",
                    imageUrl == null ? "" : imageUrl,
                    "followerCount",
                    followerCount,
                    "photoCount",
                    photoCount,
                    "bio",
                    bio == null ? "" : bio)));
  }

  @Override
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
    write(
        "upsert photo " + photoId,
        session ->
            session.run(
                """
                MATCH (u:User {userId: $userId})
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
                MERGE (p)-[:POSTED_BY]->(u)
                """,
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
                    caption == null ? "" : caption,
                    "tags",
                    tags == null ? List.of() : tags,
                    "likeCount",
                    likeCount,
                    "commentCount",
                    commentCount,
                    "shareCount",
                    shareCount,
                    "createdAt",
                    createdAt.toEpochMilli())));
  }

  @Override
  public void createFollowRelationship(String followerId, String followingId) {
    write(
        "create follow " + followerId + " -> " + followingId,
        session ->
            session.run(
                """
                MATCH (follower:User {userId: $followerId})
                MATCH (following:User {userId: $followingId})
                MERGE (follower)-[r:FOLLOWS]->(following)
                ON CREATE SET r.followedAt = timestamp()
                """,
                Values.parameters("followerId", followerId, "followingId", followingId)));
  }

  @Override
  public void removeFollowRelationship(String followerId, String followingId) {
    write(
        "remove follow " + followerId + " -> " + followingId,
        session ->
            session.run(
                """
                MATCH (:User {userId: $followerId})-[r:FOLLOWS]->(:User {userId: $followingId})
                DELETE r
                """,
                Values.parameters("followerId", followerId, "followingId", followingId)));
  }

  @Override
  public void createLikeRelationship(String userId, String photoId) {
    write(
        "create like " + userId + " -> " + photoId,
        session ->
            session.run(
                """
                MATCH (u:User {userId: $userId}), (p:Photo {photoId: $photoId})
                MERGE (u)-[r:LIKED]->(p)
                ON CREATE SET r.createdAt = timestamp()
                """,
                Values.parameters("userId", userId, "photoId", photoId)));
  }

  @Override
  public void removeLikeRelationship(String userId, String photoId) {
    write(
        "remove like " + userId + " -> " + photoId,
        session ->
            session.run(
                """
                MATCH (:User {userId: $userId})-[r:LIKED]->(:Photo {photoId: $photoId})
                DELETE r
                """,
                Values.parameters("userId", userId, "photoId", photoId)));
  }

  @Override
  public void createCommentRelationship(String userId, String photoId) {
    write(
        "create comment affinity " + userId + " -> " + photoId,
        session ->
            session.run(
                """
                MATCH (u:User {userId: $userId}), (p:Photo {photoId: $photoId})
                MERGE (u)-[r:COMMENTED]->(p)
                ON CREATE SET r.createdAt = timestamp()
                """,
                Values.parameters("userId", userId, "photoId", photoId)));
  }

  @Override
  public List<GraphAffinity> getAuthorAffinities(String viewerId, List<String> authorIds) {
    if (authorIds.isEmpty()) return List.of();
    String cypher =
        """
        UNWIND $authorIds AS authorId
        MATCH (viewer:User {userId: $viewerId})
        MATCH (author:User {userId: authorId})
        OPTIONAL MATCH (viewer)-[direct:FOLLOWS]->(author)
        WITH viewer, author, authorId, count(direct) > 0 AS followed
        OPTIONAL MATCH (viewer)-[:FOLLOWS]->(mutual:User)-[:FOLLOWS]->(author)
        WITH viewer, author, authorId, followed, count(DISTINCT mutual) AS mutualCount
        OPTIONAL MATCH (viewer)-[interaction:LIKED|COMMENTED]->(:Photo)-[:POSTED_BY]->(author)
        RETURN authorId, followed, mutualCount, count(DISTINCT interaction) AS interactionCount
        ORDER BY authorId
        """;
    try (Session session = driver.session()) {
      Result result =
          session.run(cypher, Values.parameters("viewerId", viewerId, "authorIds", authorIds));
      List<GraphAffinity> affinities = new ArrayList<>();
      while (result.hasNext()) {
        Record row = result.next();
        affinities.add(
            new GraphAffinity(
                row.get("authorId").asString(),
                row.get("followed").asBoolean(),
                row.get("mutualCount").asLong(),
                row.get("interactionCount").asLong()));
      }
      return List.copyOf(affinities);
    }
  }

  @Override
  public List<GraphFeedItem> getSuggestedUsers(String viewerId, int limit) {
    String cypher =
        """
        MATCH (viewer:User {userId: $viewerId})-[:FOLLOWS]->(mutual:User)-[:FOLLOWS]->(candidate:User)
        WHERE candidate.userId <> $viewerId
          AND NOT (viewer)-[:FOLLOWS]->(candidate)
        WITH candidate, count(DISTINCT mutual) AS mutualCount
        RETURN candidate.userId AS userId,
               mutualCount,
               coalesce(candidate.followerCount, 0) AS followerCount
        ORDER BY mutualCount DESC, followerCount DESC, userId
        LIMIT $limit
        """;
    try (Session session = driver.session()) {
      Result result = session.run(cypher, Values.parameters("viewerId", viewerId, "limit", limit));
      List<GraphFeedItem> suggestions = new ArrayList<>();
      while (result.hasNext()) {
        Record row = result.next();
        suggestions.add(
            new GraphFeedItem(row.get("userId").asString(), row.get("mutualCount").asDouble()));
      }
      return List.copyOf(suggestions);
    }
  }

  @Override
  public Map<String, Long> getGraphStats() {
    try (Session session = driver.session()) {
      Record row =
          session
              .run(
                  """
                  CALL { MATCH (u:User) RETURN count(u) AS users }
                  CALL { MATCH (p:Photo) RETURN count(p) AS photos }
                  CALL { MATCH ()-[f:FOLLOWS]->() RETURN count(f) AS follows }
                  CALL { MATCH ()-[l:LIKED]->() RETURN count(l) AS likes }
                  RETURN users, photos, follows, likes
                  """)
              .single();
      return Map.of(
          "users", row.get("users").asLong(),
          "photos", row.get("photos").asLong(),
          "follows", row.get("follows").asLong(),
          "likes", row.get("likes").asLong());
    }
  }

  private void write(String operation, Consumer<Session> action) {
    try (Session session = driver.session()) {
      action.accept(session);
    } catch (RuntimeException exception) {
      // ponytail: best-effort projection; add an outbox when guaranteed delivery matters.
      log.error("Neo4j projection failed: {}", operation, exception);
    }
  }

  public Neo4jGraphAdapter(Driver driver) {
    this.driver = driver;
  }
}
