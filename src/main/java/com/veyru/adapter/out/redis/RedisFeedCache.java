package com.veyru.adapter.out.redis;

import com.veyru.application.port.out.FeedCache;
import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisFeedCache implements FeedCache {
  private static final String PREFIX = "newsfeed:user:";
  private final RedisTemplate<String, Object> redis;

  @SuppressWarnings("unchecked")
  public List<String> get(String id) {
    return (List<String>) redis.opsForValue().get(id);
  }

  public void put(String id, List<String> values, Duration ttl) {
    redis.opsForValue().set(id, values, ttl);
  }

  public RedisFeedCache(RedisTemplate<String, Object> redis) {
    this.redis = redis;
  }
}
