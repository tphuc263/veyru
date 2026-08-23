package com.veyru.application.port.out;

import java.time.Duration;
import java.util.List;

public interface FeedCache {
  List<String> get(String userId);

  void put(String userId, List<String> photoIds, Duration ttl);
}
