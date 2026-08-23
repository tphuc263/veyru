package com.veyru.adapter.in.web;

import com.veyru.application.discovery.GraphFeedService;
import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/feed")
public class GraphFeedController {
  private static final Logger log = LoggerFactory.getLogger(GraphFeedController.class);
  private final GraphFeedService graphFeedService;

  @GetMapping("/graph")
  public ResponseEntity<List<PhotoResponse>> getGraphFeed(
      @RequestParam(defaultValue = "20") int limit) {
    log.info("Graph feed requested with limit: {}", limit);
    List<PhotoResponse> feed =
        graphFeedService.getGraphBasedFeed(limit).stream().map(PhotoResponse::from).toList();
    return ResponseEntity.ok(feed);
  }

  @GetMapping("/weighted")
  public ResponseEntity<List<PhotoResponse>> getWeightedPathFeed(
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "7") int daysBack) {
    log.info("Weighted path feed requested");
    List<PhotoResponse> feed =
        graphFeedService.getWeightedPathFeed(limit, daysBack).stream()
            .map(PhotoResponse::from)
            .toList();
    return ResponseEntity.ok(feed);
  }

  @GetMapping("/hybrid")
  public ResponseEntity<List<PhotoResponse>> getHybridFeed(
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0.5") double alpha) {
    log.info("Hybrid feed requested with alpha: {}", alpha);
    List<PhotoResponse> feed =
        graphFeedService.getHybridFeed(limit, alpha).stream()
            .map(PhotoResponse::from)
            .toList();
    return ResponseEntity.ok(feed);
  }

  @GetMapping("/suggestions")
  public ResponseEntity<List<String>> getSuggestedUsers(
      @RequestParam(defaultValue = "10") int limit) {
    List<String> suggestions = graphFeedService.getSuggestedUsers(limit);
    return ResponseEntity.ok(suggestions);
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
      if (Double.compare(this.getGraphWeightedOverlap(), other.getGraphWeightedOverlap()) != 0)
        return false;
      if (Double.compare(this.getGraphHybridOverlap(), other.getGraphHybridOverlap()) != 0)
        return false;
      if (Double.compare(this.getGraphTraditionalOverlap(), other.getGraphTraditionalOverlap())
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
      final long $graphWeightedOverlap = Double.doubleToLongBits(this.getGraphWeightedOverlap());
      result = result * PRIME + (int) ($graphWeightedOverlap >>> 32 ^ $graphWeightedOverlap);
      final long $graphHybridOverlap = Double.doubleToLongBits(this.getGraphHybridOverlap());
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

  public GraphFeedController(final GraphFeedService graphFeedService) {
    this.graphFeedService = graphFeedService;
  }
}
