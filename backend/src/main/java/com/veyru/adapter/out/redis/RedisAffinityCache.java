package com.veyru.adapter.out.redis;

import com.veyru.application.port.out.AffinityCache;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisAffinityCache implements AffinityCache {
  private static final String PREFIX = "feed:affinity:v1:";
  private final RedisTemplate<String, Object> redis;

  @Override
  public Optional<Map<String, Double>> get(String viewerId) {
    Object value = redis.opsForValue().get(key(viewerId));
    if (!(value instanceof Map<?, ?> raw)) return Optional.empty();
    Map<String, Double> affinities = new LinkedHashMap<>();
    raw.forEach(
        (authorId, score) -> {
          if (authorId instanceof String id && score instanceof Number number) {
            affinities.put(id, number.doubleValue());
          }
        });
    return Optional.of(Map.copyOf(affinities));
  }

  @Override
  public void put(String viewerId, Map<String, Double> affinities, Duration ttl) {
    redis.opsForValue().set(key(viewerId), affinities, ttl);
  }

  @Override
  public void evict(String viewerId) {
    redis.delete(key(viewerId));
  }

  private String key(String viewerId) {
    return PREFIX + viewerId;
  }

  public RedisAffinityCache(RedisTemplate<String, Object> redis) {
    this.redis = redis;
  }
}
