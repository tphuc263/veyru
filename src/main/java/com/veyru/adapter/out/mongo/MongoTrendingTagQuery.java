package com.veyru.adapter.out.mongo;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import com.veyru.application.port.out.TrendingTagQuery;
import java.time.Instant;
import java.util.List;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

@Component
public class MongoTrendingTagQuery implements TrendingTagQuery {
  private final MongoTemplate mongoTemplate;

  public MongoTrendingTagQuery(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public List<String> findTrendingSince(Instant since, int maxResults) {
    Aggregation aggregation =
        Aggregation.newAggregation(
            match(Criteria.where("createdAt").gte(since)),
            unwind("tags"),
            group("tags").count().as("count"),
            sort(Sort.by(Sort.Direction.DESC, "count")),
            limit(maxResults),
            project().and("_id").as("tag"));
    return mongoTemplate.aggregate(aggregation, "photos", Document.class).getMappedResults().stream()
        .map(result -> result.getString("tag"))
        .toList();
  }
}
