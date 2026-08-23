package com.veyru.application.port.out;

import com.veyru.application.discovery.GraphFeedItem;
import java.util.List;
import java.util.Map;

public interface GraphFeedQuery {
  List<GraphFeedItem> getFeedWithDijkstra(String userId, int limit);

  List<GraphFeedItem> getWeightedPathFeed(String userId, int limit, int daysBack);

  List<String> getSuggestedUsersFromGraph(String userId, int limit);

  Map<String, Long> getGraphStats();
}
