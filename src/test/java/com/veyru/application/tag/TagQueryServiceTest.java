package com.veyru.application.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class TagQueryServiceTest {
  @Test
  void queriesTagsFromTheLastSevenDays() {
    Instant now = Instant.parse("2026-08-23T00:00:00Z");
    AtomicReference<Instant> since = new AtomicReference<>();
    AtomicInteger maxResults = new AtomicInteger();
    TagQueryService service =
        new TagQueryService(
            (start, limit) -> {
              since.set(start);
              maxResults.set(limit);
              return List.of("java");
            },
            Clock.fixed(now, ZoneOffset.UTC));

    assertThat(service.getTrendingHashtags(10)).containsExactly("java");
    assertThat(since.get()).isEqualTo(Instant.parse("2026-08-16T00:00:00Z"));
    assertThat(maxResults.get()).isEqualTo(10);
  }
}
