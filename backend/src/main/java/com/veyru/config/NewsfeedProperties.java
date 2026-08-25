package com.veyru.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "newsfeed")
public record NewsfeedProperties(Ranking ranking, Cache cache) {
  public record Ranking(
      int candidateLimit,
      int lookbackDays,
      double graphWeight,
      double directFollowWeight,
      double interactionWeight,
      double mutualWeight,
      double recencyWeight,
      double engagementWeight,
      double qualityWeight,
      double recencyDecayHours,
      double engagementScale) {}

  public record Cache(Duration affinityTtl, Duration cursorTtl) {}
}
