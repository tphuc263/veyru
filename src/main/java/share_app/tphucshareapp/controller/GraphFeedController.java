package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.service.graph.GraphFeedService;
import share_app.tphucshareapp.service.graph.GraphSyncService;
import share_app.tphucshareapp.service.photo.INewsfeedService;
import share_app.tphucshareapp.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Slf4j
public class GraphFeedController {

    private final GraphFeedService graphFeedService;
    private final GraphSyncService graphSyncService;
    private final INewsfeedService newsfeedService;
    private final UserService userService;

    @GetMapping("/graph")
    public ResponseEntity<List<PhotoResponse>> getGraphFeed(
            @RequestParam(defaultValue = "20") int limit) {

        String userId = userService.getCurrentUser().getId();
        log.info("Graph feed requested by user: {} with limit: {}", userId, limit);

        List<PhotoResponse> feed = graphFeedService.getGraphBasedFeed(userId, limit);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/weighted")
    public ResponseEntity<List<PhotoResponse>> getWeightedPathFeed(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "7") int daysBack) {

        String userId = userService.getCurrentUser().getId();
        log.info("Weighted path feed requested by user: {}", userId);

        List<PhotoResponse> feed = graphFeedService.getWeightedPathFeed(userId, limit, daysBack);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/hybrid")
    public ResponseEntity<List<PhotoResponse>> getHybridFeed(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0.5") double alpha) {

        String userId = userService.getCurrentUser().getId();
        log.info("Hybrid feed requested by user: {} with alpha: {}", userId, alpha);

        List<PhotoResponse> feed = graphFeedService.getHybridFeed(userId, limit, alpha);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestedUsers(
            @RequestParam(defaultValue = "10") int limit) {

        String userId = userService.getCurrentUser().getId();
        List<String> suggestions = graphFeedService.getSuggestedUsers(userId, limit);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> triggerFullSync() {
        log.info("Full sync to Neo4j triggered");

        try {
            String result = graphSyncService.performFullSync();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Full sync failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Sync failed: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getGraphStats() {
        String stats = graphFeedService.getGraphStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/compare")
    public ResponseEntity<FeedComparisonResponse> compareFeeds(
            @RequestParam(defaultValue = "10") int limit) {

        String userId = userService.getCurrentUser().getId();
        log.info("Feed comparison requested by user: {}", userId);

        List<PhotoResponse> graphFeed = graphFeedService.getGraphBasedFeed(userId, limit);
        List<PhotoResponse> weightedFeed = graphFeedService.getWeightedPathFeed(userId, limit, 7);
        List<PhotoResponse> hybridFeed = graphFeedService.getHybridFeed(userId, limit, 0.5);
        Page<PhotoResponse> traditionalFeed = newsfeedService.getNewsfeed(userId, 0, limit);

        FeedComparisonResponse response = new FeedComparisonResponse();
        response.setGraphFeed(graphFeed);
        response.setWeightedFeed(weightedFeed);
        response.setHybridFeed(hybridFeed);
        response.setTraditionalFeed(traditionalFeed.getContent());
        response.setGraphWeightedOverlap(calculateOverlap(graphFeed, weightedFeed));
        response.setGraphHybridOverlap(calculateOverlap(graphFeed, hybridFeed));
        response.setGraphTraditionalOverlap(calculateOverlap(graphFeed, traditionalFeed.getContent()));

        return ResponseEntity.ok(response);
    }

    private double calculateOverlap(List<PhotoResponse> feed1, List<PhotoResponse> feed2) {
        if (feed1.isEmpty() || feed2.isEmpty()) {
            return 0.0;
        }
        List<String> ids1 = feed1.stream().map(PhotoResponse::getId).toList();
        List<String> ids2 = feed2.stream().map(PhotoResponse::getId).toList();
        long overlap = ids1.stream().filter(ids2::contains).count();
        return (double) overlap / Math.min(ids1.size(), ids2.size()) * 100;
    }

    @lombok.Data
    public static class FeedComparisonResponse {
        private List<PhotoResponse> graphFeed;
        private List<PhotoResponse> weightedFeed;
        private List<PhotoResponse> hybridFeed;
        private List<PhotoResponse> traditionalFeed;
        private double graphWeightedOverlap;
        private double graphHybridOverlap;
        private double graphTraditionalOverlap;
    }
}

