package com.veyru.config;

import com.veyru.application.port.out.TrendingTagQuery;
import com.veyru.application.tag.TagQueryService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TagConfig {
  @Bean
  public TagQueryService tagQueryService(TrendingTagQuery trendingTags) {
    return new TagQueryService(trendingTags, Clock.systemUTC());
  }
}
