package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.domain.service.graph.GraphFeedService;
import com.veyru.domain.service.graph.GraphSyncService;
import com.veyru.domain.service.photo.NewsfeedService;
import com.veyru.domain.service.user.UserService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feed")
public class GraphFeedController {
  private static final Logger log = LoggerFactory.getLogger(GraphFeedController.class);
  private final GraphFeedService graphFeedService;
  private final GraphSyncService graphSyncService;
  private final NewsfeedService newsfeedService;
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
      return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
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

  public static class FeedComparisonResponse {
    private List<PhotoResponse> graphFeed;
    private List<PhotoResponse> weightedFeed;
    private List<PhotoResponse> hybridFeed;
    private List<PhotoResponse> traditionalFeed;
    private double graphWeightedOverlap;
    private double graphHybridOverlap;
    private double graphTraditionalOverlap;

    public FeedComparisonResponse() {}

    public List<PhotoResponse> getGraphFeed() {
      return this.graphFeed;
    }

    public List<PhotoResponse> getWeightedFeed() {
      return this.weightedFeed;
    }

    public List<PhotoResponse> getHybridFeed() {
      return this.hybridFeed;
    }

    public List<PhotoResponse> getTraditionalFeed() {
      return this.traditionalFeed;
    }

    public double getGraphWeightedOverlap() {
      return this.graphWeightedOverlap;
    }

    public double getGraphHybridOverlap() {
      return this.graphHybridOverlap;
    }

    public double getGraphTraditionalOverlap() {
      return this.graphTraditionalOverlap;
    }

    public void setGraphFeed(final List<PhotoResponse> graphFeed) {
      this.graphFeed = graphFeed;
    }

    public void setWeightedFeed(final List<PhotoResponse> weightedFeed) {
      this.weightedFeed = weightedFeed;
    }

    public void setHybridFeed(final List<PhotoResponse> hybridFeed) {
      this.hybridFeed = hybridFeed;
    }

    public void setTraditionalFeed(final List<PhotoResponse> traditionalFeed) {
      this.traditionalFeed = traditionalFeed;
    }

    public void setGraphWeightedOverlap(final double graphWeightedOverlap) {
      this.graphWeightedOverlap = graphWeightedOverlap;
    }

    public void setGraphHybridOverlap(final double graphHybridOverlap) {
      this.graphHybridOverlap = graphHybridOverlap;
    }

    public void setGraphTraditionalOverlap(final double graphTraditionalOverlap) {
      this.graphTraditionalOverlap = graphTraditionalOverlap;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof GraphFeedController.FeedComparisonResponse)) return false;
      final GraphFeedController.FeedComparisonResponse other =
          (GraphFeedController.FeedComparisonResponse) o;
      if (!other.canEqual((Object) this)) return false;
      if (Double.compare(this.getGraphWeightedOverlap(), other.getGraphWeightedOverlap())
          != 0) return false;
      if (Double.compare(this.getGraphHybridOverlap(), other.getGraphHybridOverlap())
          != 0) return false;
      if (Double.compare(
              this.getGraphTraditionalOverlap(), other.getGraphTraditionalOverlap())
          != 0) return false;
      final Object this$graphFeed = this.getGraphFeed();
      final Object other$graphFeed = other.getGraphFeed();
      if (this$graphFeed == null
          ? other$graphFeed != null
          : !this$graphFeed.equals(other$graphFeed)) return false;
      final Object this$weightedFeed = this.getWeightedFeed();
      final Object other$weightedFeed = other.getWeightedFeed();
      if (this$weightedFeed == null
          ? other$weightedFeed != null
          : !this$weightedFeed.equals(other$weightedFeed)) return false;
      final Object this$hybridFeed = this.getHybridFeed();
      final Object other$hybridFeed = other.getHybridFeed();
      if (this$hybridFeed == null
          ? other$hybridFeed != null
          : !this$hybridFeed.equals(other$hybridFeed)) return false;
      final Object this$traditionalFeed = this.getTraditionalFeed();
      final Object other$traditionalFeed = other.getTraditionalFeed();
      if (this$traditionalFeed == null
          ? other$traditionalFeed != null
          : !this$traditionalFeed.equals(other$traditionalFeed)) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof GraphFeedController.FeedComparisonResponse;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final long $graphWeightedOverlap =
          Double.doubleToLongBits(this.getGraphWeightedOverlap());
      result = result * PRIME + (int) ($graphWeightedOverlap >>> 32 ^ $graphWeightedOverlap);
      final long $graphHybridOverlap =
          Double.doubleToLongBits(this.getGraphHybridOverlap());
      result = result * PRIME + (int) ($graphHybridOverlap >>> 32 ^ $graphHybridOverlap);
      final long $graphTraditionalOverlap =
          Double.doubleToLongBits(this.getGraphTraditionalOverlap());
      result = result * PRIME + (int) ($graphTraditionalOverlap >>> 32 ^ $graphTraditionalOverlap);
      final Object $graphFeed = this.getGraphFeed();
      result = result * PRIME + ($graphFeed == null ? 43 : $graphFeed.hashCode());
      final Object $weightedFeed = this.getWeightedFeed();
      result = result * PRIME + ($weightedFeed == null ? 43 : $weightedFeed.hashCode());
      final Object $hybridFeed = this.getHybridFeed();
      result = result * PRIME + ($hybridFeed == null ? 43 : $hybridFeed.hashCode());
      final Object $traditionalFeed = this.getTraditionalFeed();
      result = result * PRIME + ($traditionalFeed == null ? 43 : $traditionalFeed.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "GraphFeedController.FeedComparisonResponse(graphFeed="
          + this.getGraphFeed()
          + ", weightedFeed="
          + this.getWeightedFeed()
          + ", hybridFeed="
          + this.getHybridFeed()
          + ", traditionalFeed="
          + this.getTraditionalFeed()
          + ", graphWeightedOverlap="
          + this.getGraphWeightedOverlap()
          + ", graphHybridOverlap="
          + this.getGraphHybridOverlap()
          + ", graphTraditionalOverlap="
          + this.getGraphTraditionalOverlap()
          + ")";
    }
  }

  public GraphFeedController(
      final GraphFeedService graphFeedService,
      final GraphSyncService graphSyncService,
      final NewsfeedService newsfeedService,
      final UserService userService) {
    this.graphFeedService = graphFeedService;
    this.graphSyncService = graphSyncService;
    this.newsfeedService = newsfeedService;
    this.userService = userService;
  }
}
