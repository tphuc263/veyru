package com.veyru.application.tag;

import com.veyru.application.port.out.TrendingTagQuery;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TagQueryService {
  private final TrendingTagQuery trendingTags;
  private final Clock clock;

  public TagQueryService(TrendingTagQuery trendingTags, Clock clock) {
    this.trendingTags = trendingTags;
    this.clock = clock;
  }

  public List<String> getTrendingHashtags(int limit) {
    return trendingTags.findTrendingSince(clock.instant().minus(7, ChronoUnit.DAYS), limit);
  }

  public List<String> getPopularHashtags(int limit) {
    return getTrendingHashtags(limit);
  }
}
