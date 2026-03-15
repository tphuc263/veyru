package share_app.tphucshareapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final MongoTemplate mongoTemplate;

    /**
     * Get trending hashtags from the last 7 days
     * Based on photo count per tag
     */
    public List<String> getTrendingHashtags(int limit) {
        log.info("Fetching trending hashtags with limit: {}", limit);

        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        Aggregation aggregation = Aggregation.newAggregation(
                // Match photos created in last 7 days
                match(Criteria.where("createdAt").gte(sevenDaysAgo)),
                // Unwind tags array
                unwind("tags"),
                // Group by tag and count
                group("tags")
                        .count()
                        .as("count"),
                // Sort by count descending
                sort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "count")),
                // Limit results
                limit(limit),
                // Project to get just the tag name
                project()
                        .and("_id").as("tag")
                        .and("count").as("usageCount")
        );

        AggregationResults<TrendingTagResult> results = mongoTemplate.aggregate(
                aggregation,
                "photos",
                TrendingTagResult.class
        );

        return results.getMappedResults().stream()
                .map(TrendingTagResult::getTag)
                .toList();
    }

    /**
     * Get popular hashtags for a specific user based on their interests
     */
    public List<String> getPopularHashtags(int limit) {
        return getTrendingHashtags(limit);
    }

    /**
     * Simple result class for aggregation
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TrendingTagResult {
        private String tag;
        private long usageCount;
    }
}

