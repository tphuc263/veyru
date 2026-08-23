package com.veyru.config;

import com.veyru.application.port.out.TrendingTagQuery;
import com.veyru.application.tag.TagQueryService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TagConfig {
  @Bean
  public TagQueryService tagQueryService(TrendingTagQuery trendingTags, Clock clock) {
    return new TagQueryService(trendingTags, clock);
  }
}
