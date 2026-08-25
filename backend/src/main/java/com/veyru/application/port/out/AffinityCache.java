package com.veyru.application.port.out;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface AffinityCache {
  Optional<Map<String, Double>> get(String viewerId);

  void put(String viewerId, Map<String, Double> affinities, Duration ttl);

  void evict(String viewerId);
}
