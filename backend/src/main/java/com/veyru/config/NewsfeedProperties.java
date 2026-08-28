package com.veyru.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "newsfeed")
public record NewsfeedProperties(@NotNull @Valid Ranking ranking, @NotNull @Valid Cache cache) {
  public record Ranking(
      @Min(1) int candidateLimit,
      @Min(1) int lookbackDays,
      @DecimalMin("0.0") @DecimalMax("1.0") double graphWeight,
      @DecimalMin("0.0") @DecimalMax("1.0") double directFollowWeight,
      @DecimalMin("0.0") @DecimalMax("1.0") double interactionWeight,
      @DecimalMin("0.0") @DecimalMax("1.0") double mutualWeight,
      @DecimalMin("0.0") @DecimalMax("1.0") double recencyWeight,
      @DecimalMin("0.0") @DecimalMax("1.0") double engagementWeight,
      @DecimalMin("0.0") @DecimalMax("1.0") double qualityWeight,
      @Positive double recencyDecayHours,
      @Positive double engagementScale) {}

  public record Cache(@NotNull Duration affinityTtl, @NotNull Duration cursorTtl) {
    public Cache {
      if (affinityTtl != null && (affinityTtl.isZero() || affinityTtl.isNegative())) {
        throw new IllegalArgumentException("newsfeed.cache.affinity-ttl must be positive");
      }
      if (cursorTtl != null && (cursorTtl.isZero() || cursorTtl.isNegative())) {
        throw new IllegalArgumentException("newsfeed.cache.cursor-ttl must be positive");
      }
    }
  }
}
