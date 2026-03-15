package share_app.tphucshareapp.service.graph;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.service.photo.PhotoConversionService;
import share_app.tphucshareapp.service.user.UserService;

/**
 * Graph-based Feed Service
 * Uses Neo4j for advanced feed ranking with Dijkstra algorithm
 * This is an alternative/complement to the existing NewsfeedService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphFeedService {

    private final Neo4jGraphService neo4jGraphService;
    private final PhotoRepository photoRepository;
    private final UserService userService;
    private final PhotoConversionService photoConversionService;

    /**
     * Get personalized feed using Neo4j graph-based ranking
     * Uses weighted path algorithm similar to Dijkstra
     * 
     * Advantages over traditional ranking:
     * - Considers user's engagement patterns
     * - Uses graph traversal for better personalization
     * - Can find "similar" users through mutual connections
     */
    public List<PhotoResponse> getGraphBasedFeed(String userId, int limit) {
        log.info("Getting graph-based feed for user: {} with limit: {}", userId, limit);

        try {
            // Get feed from Neo4j using Dijkstra-like algorithm
            List<Neo4jGraphService.FeedNode> feedNodes = neo4jGraphService.getFeedWithDijkstra(userId, limit);

            if (feedNodes.isEmpty()) {
                log.info("No graph-based feed found, returning empty list");
                return List.of();
            }

            // Fetch full Photo entities from MongoDB
            List<String> photoIds = feedNodes.stream()
                    .map(Neo4jGraphService.FeedNode::getPhotoId)
                    .toList();

            List<Photo> photos = photoRepository.findAllById(photoIds);

            // Sort by relevance score from Neo4j
            photos.sort((a, b) -> {
                double scoreA = feedNodes.stream()
                        .filter(n -> n.getPhotoId().equals(a.getId()))
                        .findFirst()
                        .map(Neo4jGraphService.FeedNode::getRelevanceScore)
                        .orElse(0.0);
                double scoreB = feedNodes.stream()
                        .filter(n -> n.getPhotoId().equals(b.getId()))
                        .findFirst()
                        .map(Neo4jGraphService.FeedNode::getRelevanceScore)
                        .orElse(0.0);
                return Double.compare(scoreB, scoreA);
            });

            // Convert to response
            User currentUser = userService.findUserById(userId);
            return photos.stream()
                    .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
                    .toList();

        } catch (Exception e) {
            log.error("Error getting graph-based feed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get feed using weighted shortest path algorithm
     * This is more sophisticated and considers:
     * - Direct follows
     * - Mutual engagement patterns
     * - Content similarity (tag overlap)
     * - Author popularity
     */
    public List<PhotoResponse> getWeightedPathFeed(String userId, int limit, int daysBack) {
        log.info("Getting weighted path feed for user: {}", userId);

        try {
            List<Neo4jGraphService.FeedNode> feedNodes = neo4jGraphService.getWeightedPathFeed(userId, limit, daysBack);

            if (feedNodes.isEmpty()) {
                return List.of();
            }

            // Fetch and convert photos
            List<String> photoIds = feedNodes.stream()
                    .map(Neo4jGraphService.FeedNode::getPhotoId)
                    .toList();

            List<Photo> photos = photoRepository.findAllById(photoIds);

            // Sort by relevance
            photos.sort((a, b) -> {
                double scoreA = feedNodes.stream()
                        .filter(n -> n.getPhotoId().equals(a.getId()))
                        .findFirst()
                        .map(Neo4jGraphService.FeedNode::getRelevanceScore)
                        .orElse(0.0);
                double scoreB = feedNodes.stream()
                        .filter(n -> n.getPhotoId().equals(b.getId()))
                        .findFirst()
                        .map(Neo4jGraphService.FeedNode::getRelevanceScore)
                        .orElse(0.0);
                return Double.compare(scoreB, scoreA);
            });

            User currentUser = userService.findUserById(userId);
            return photos.stream()
                    .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
                    .toList();

        } catch (Exception e) {
            log.error("Error getting weighted path feed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Hybrid approach: Combine graph-based ranking with traditional ranking
     * 
     * Formula: finalScore = alpha * graphScore + (1 - alpha) * traditionalScore
     * 
     * @param userId Current user ID
     * @param limit  Number of photos to return
     * @param alpha  Weight for graph score (0.0 to 1.0). Higher = more personalized
     */
    public List<PhotoResponse> getHybridFeed(String userId, int limit, double alpha) {
        log.info("Getting hybrid feed for user: {} with alpha: {}", userId, alpha);

        // Get graph-based scores
        List<Neo4jGraphService.FeedNode> graphNodes = neo4jGraphService.getFeedWithDijkstra(userId, limit * 2); // Get
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
        Instant cutoffTime = Instant.now().minus(Duration.ofDays(30));
        List<Photo> photos = photoRepository.findByUser_UserIdInAndCreatedAtAfterOrderByCreatedAtDesc(
                followingIds, cutoffTime);

        if (photos.isEmpty()) {
            photos = photoRepository.findByUser_UserIdInOrderByCreatedAtDesc(followingIds);
        }

        if (photos.isEmpty()) {
            return List.of();
        }

        // Calculate hybrid scores
        User currentUser = userService.findUserById(userId);

        // Create hybrid scores and sort
        List<PhotoWithHybridScore> scoredPhotos = photos.stream()
                .map(photo -> {
                    double graphScore = graphNodes.stream()
                            .filter(n -> n.getPhotoId().equals(photo.getId()))
                            .findFirst()
                            .map(Neo4jGraphService.FeedNode::getRelevanceScore)
                            .orElse(0.0);

                    double traditionalScore = calculateTraditionalScore(photo);

                    double hybridScore = alpha * graphScore + (1 - alpha) * traditionalScore;

                    return new PhotoWithHybridScore(photo, hybridScore);
                })
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(limit)
                .toList();

        // Convert to response
        List<PhotoResponse> result = scoredPhotos.stream()
                .map(ps -> photoConversionService.convertToPhotoResponse(ps.photo, currentUser))
                .toList();

        return result;
    }

    /**
     * Traditional score calculation (same as in NewsfeedService)
     */
    private double calculateTraditionalScore(Photo photo) {
        double score = 0.0;
        long hoursOld = java.time.Duration.between(photo.getCreatedAt(), java.time.Instant.now()).toHours();

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
     * Get suggested users based on graph analysis
     * Finds users who are popular among people you follow
     */
    public List<String> getSuggestedUsers(String userId, int limit) {
        return neo4jGraphService.getSuggestedUsersFromGraph(userId, limit);
    }

    /**
     * Get graph statistics for monitoring
     */
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
}
