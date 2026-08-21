package com.veyru.domain.service;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class TagService {
  private static final Logger log = LoggerFactory.getLogger(TagService.class);
  private final MongoTemplate mongoTemplate;

  /** Get trending hashtags from the last 7 days Based on photo count per tag */
  public List<String> getTrendingHashtags(int limit) {
    log.info("Fetching trending hashtags with limit: {}", limit);
    Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
    Aggregation aggregation =
        Aggregation.newAggregation(
            // Match photos created in last 7 days
            match(Criteria.where("createdAt").gte(sevenDaysAgo)),
            // Unwind tags array
            unwind("tags"),
            // Group by tag and count
            group("tags").count().as("count"),
            // Sort by count descending
            sort(
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "count")),
            // Limit results
            limit(limit),
            // Project to get just the tag name
            project().and("_id").as("tag").and("count").as("usageCount"));
    AggregationResults<TrendingTagResult> results =
        mongoTemplate.aggregate(aggregation, "photos", TrendingTagResult.class);
    return results.getMappedResults().stream().map(TrendingTagResult::getTag).toList();
  }

  /** Get popular hashtags for a specific user based on their interests */
  public List<String> getPopularHashtags(int limit) {
    return getTrendingHashtags(limit);
  }

  /** Simple result class for aggregation */
  public static class TrendingTagResult {
    private String tag;
    private long usageCount;

    public String getTag() {
      return this.tag;
    }

    public long getUsageCount() {
      return this.usageCount;
    }

    public void setTag(final String tag) {
      this.tag = tag;
    }

    public void setUsageCount(final long usageCount) {
      this.usageCount = usageCount;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof TagService.TrendingTagResult)) return false;
      final TagService.TrendingTagResult other = (TagService.TrendingTagResult) o;
      if (!other.canEqual((Object) this)) return false;
      if (this.getUsageCount() != other.getUsageCount()) return false;
      final Object this$tag = this.getTag();
      final Object other$tag = other.getTag();
      if (this$tag == null ? other$tag != null : !this$tag.equals(other$tag)) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof TagService.TrendingTagResult;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final long $usageCount = this.getUsageCount();
      result = result * PRIME + (int) ($usageCount >>> 32 ^ $usageCount);
      final Object $tag = this.getTag();
      result = result * PRIME + ($tag == null ? 43 : $tag.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "TagService.TrendingTagResult(tag="
          + this.getTag()
          + ", usageCount="
          + this.getUsageCount()
          + ")";
    }

    public TrendingTagResult(final String tag, final long usageCount) {
      this.tag = tag;
      this.usageCount = usageCount;
    }
  }

  public TagService(final MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }
}
