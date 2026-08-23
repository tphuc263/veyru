package com.veyru.application.port.out;

import java.time.Instant;
import java.util.List;

public interface TrendingTagQuery {
  List<String> findTrendingSince(Instant since, int limit);
}
