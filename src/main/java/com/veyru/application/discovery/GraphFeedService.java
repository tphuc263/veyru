package com.veyru.application.discovery;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.GraphFeedQuery;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graph-based Feed Service Uses Neo4j for advanced feed ranking with Dijkstra algorithm This is an
 * alternative/complement to the existing NewsfeedService
 */
public class GraphFeedService {
  private static final Logger log = LoggerFactory.getLogger(GraphFeedService.class);
  private final GraphFeedQuery neo4jGraphService;
  private final PhotoStore photoStore;
  private final UserProfileService userService;
  private final PhotoConversionService photoConversionService;
  private final Clock clock;

  /**
   * Get personalized feed using Neo4j graph-based ranking Uses weighted path algorithm similar to
   * Dijkstra
   *
   * <p>Advantages over traditional ranking: - Considers user's engagement patterns - Uses graph
   * traversal for better personalization - Can find "similar" users through mutual connections
   */
  public List<PhotoResult> getGraphBasedFeed(String userId, int limit) {
    log.info("Getting graph-based feed for user: {} with limit: {}", userId, limit);

    // Get feed from Neo4j using Dijkstra-like algorithm
    List<GraphFeedItem> feedNodes = neo4jGraphService.getFeedWithDijkstra(userId, limit);
    if (feedNodes.isEmpty()) {
      log.info("No graph-based feed found, returning empty list");
      return List.of();
    }
    // Fetch full Photo entities from MongoDB
    List<String> photoIds = feedNodes.stream().map(GraphFeedItem::photoId).toList();
    List<Photo> photos = photoStore.findAllById(photoIds);
    // Sort by relevance score from Neo4j
    photos.sort(
        (a, b) -> {
          double scoreA =
              feedNodes.stream()
                  .filter(n -> n.photoId().equals(a.getId()))
                  .findFirst()
                  .map(GraphFeedItem::relevanceScore)
                  .orElse(0.0);
          double scoreB =
              feedNodes.stream()
                  .filter(n -> n.photoId().equals(b.getId()))
                  .findFirst()
                  .map(GraphFeedItem::relevanceScore)
                  .orElse(0.0);
          return Double.compare(scoreB, scoreA);
        });
    // Convert to response
    User currentUser = userService.findUserById(userId);
    return photos.stream()
        .map(
            photo ->
                photoConversionService.convertToPhotoResponse(
                    photo, java.util.Optional.of(currentUser)))
        .toList();
  }

  public List<PhotoResult> getGraphBasedFeed(int limit) {
    return getGraphBasedFeed(userService.requireCurrentUserId(), limit);
  }

  /**
   * Get feed using weighted shortest path algorithm This is more sophisticated and considers: -
   * Direct follows - Mutual engagement patterns - Content similarity (tag overlap) - Author
   * popularity
   */
  public List<PhotoResult> getWeightedPathFeed(String userId, int limit, int daysBack) {
    log.info("Getting weighted path feed for user: {}", userId);

    List<GraphFeedItem> feedNodes = neo4jGraphService.getWeightedPathFeed(userId, limit, daysBack);
    if (feedNodes.isEmpty()) {
      return List.of();
    }
    // Fetch and convert photos
    List<String> photoIds = feedNodes.stream().map(GraphFeedItem::photoId).toList();
    List<Photo> photos = photoStore.findAllById(photoIds);
    // Sort by relevance
    photos.sort(
        (a, b) -> {
          double scoreA =
              feedNodes.stream()
                  .filter(n -> n.photoId().equals(a.getId()))
                  .findFirst()
                  .map(GraphFeedItem::relevanceScore)
                  .orElse(0.0);
          double scoreB =
              feedNodes.stream()
                  .filter(n -> n.photoId().equals(b.getId()))
                  .findFirst()
                  .map(GraphFeedItem::relevanceScore)
                  .orElse(0.0);
          return Double.compare(scoreB, scoreA);
        });
    User currentUser = userService.findUserById(userId);
    return photos.stream()
        .map(
            photo ->
                photoConversionService.convertToPhotoResponse(
                    photo, java.util.Optional.of(currentUser)))
        .toList();
  }

  public List<PhotoResult> getWeightedPathFeed(int limit, int daysBack) {
    return getWeightedPathFeed(userService.requireCurrentUserId(), limit, daysBack);
  }

  /**
   * Hybrid approach: Combine graph-based ranking with traditional ranking
   *
   * <p>Formula: finalScore = alpha * graphScore + (1 - alpha) * traditionalScore
   *
   * @param userId Current user ID
   * @param limit Number of photos to return
   * @param alpha Weight for graph score (0.0 to 1.0). Higher = more personalized
   */
  public List<PhotoResult> getHybridFeed(String userId, int limit, double alpha) {
    log.info("Getting hybrid feed for user: {} with alpha: {}", userId, alpha);
    // Get graph-based scores
    List<GraphFeedItem> graphNodes =
        neo4jGraphService.getFeedWithDijkstra(userId, limit * 2); // Get
    // more
    // for
    // filtering
    if (graphNodes.isEmpty()) {
      log.info("No graph data available, falling back to traditional ranking");
      return getGraphBasedFeed(userId, limit);
    }
    // Get candidate photos from followed users
    List<String> followingIds = neo4jGraphService.getSuggestedUsersFromGraph(userId, limit * 2);
    if (followingIds.isEmpty()) {
      return getGraphBasedFeed(userId, limit);
    }
    // Fetch photos from candidates
    Instant cutoffTime = clock.instant().minus(Duration.ofDays(30));
    List<Photo> photos = photoStore.findByUsersAfter(followingIds, cutoffTime);
    if (photos.isEmpty()) {
      photos = photoStore.findByUsers(followingIds);
    }
    if (photos.isEmpty()) {
      return List.of();
    }
    // Calculate hybrid scores
    User currentUser = userService.findUserById(userId);
    // Create hybrid scores and sort
    List<PhotoWithHybridScore> scoredPhotos =
        photos.stream()
            .map(
                photo -> {
                  double graphScore =
                      graphNodes.stream()
                          .filter(n -> n.photoId().equals(photo.getId()))
                          .findFirst()
                          .map(GraphFeedItem::relevanceScore)
                          .orElse(0.0);
                  double traditionalScore = calculateTraditionalScore(photo);
                  double hybridScore = alpha * graphScore + (1 - alpha) * traditionalScore;
                  return new PhotoWithHybridScore(photo, hybridScore);
                })
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(limit)
            .toList();
    // Convert to response
    List<PhotoResult> result =
        scoredPhotos.stream()
            .map(
                ps ->
                    photoConversionService.convertToPhotoResponse(
                        ps.photo, java.util.Optional.of(currentUser)))
            .toList();
    return result;
  }

  public List<PhotoResult> getHybridFeed(int limit, double alpha) {
    return getHybridFeed(userService.requireCurrentUserId(), limit, alpha);
  }

  /** Traditional score calculation (same as in NewsfeedService) */
  private double calculateTraditionalScore(Photo photo) {
    double score = 0.0;
    long hoursOld = java.time.Duration.between(photo.getCreatedAt(), clock.instant()).toHours();
    // Time decay
    if (hoursOld < 24) {
      score += 100 - (hoursOld * 2);
    } else if (hoursOld < 168) {
      score += 50 - ((hoursOld - 24) * 0.3);
    } else {
      score += Math.max(0, 10 - ((hoursOld - 168) * 0.1));
    }
    // Engagement
    score += photo.getLikeCount() * 2;
    score += photo.getCommentCount() * 5;
    // Content quality
    if (photo.getCaption() != null && !photo.getCaption().trim().isEmpty()) {
      score += 10;
    }
    if (photo.getTags() != null && !photo.getTags().isEmpty()) {
      score += 5;
    }
    return score;
  }

  /**
   * Get suggested users based on graph analysis Finds users who are popular among people you follow
   */
  public List<String> getSuggestedUsers(String userId, int limit) {
    return neo4jGraphService.getSuggestedUsersFromGraph(userId, limit);
  }

  public List<String> getSuggestedUsers(int limit) {
    return getSuggestedUsers(userService.requireCurrentUserId(), limit);
  }

  /** Get graph statistics for monitoring */
  public String getGraphStats() {
    return neo4jGraphService.getGraphStats().toString();
  }

  // Helper class for hybrid scoring
  private static class PhotoWithHybridScore {
    final Photo photo;
    final double score;

    PhotoWithHybridScore(Photo photo, double score) {
      this.photo = photo;
      this.score = score;
    }
  }

  public GraphFeedService(
      final GraphFeedQuery neo4jGraphService,
      final PhotoStore photoStore,
      final UserProfileService userService,
      final PhotoConversionService photoConversionService,
      final Clock clock) {
    this.neo4jGraphService = neo4jGraphService;
    this.photoStore = photoStore;
    this.userService = userService;
    this.photoConversionService = photoConversionService;
    this.clock = clock;
  }
}
