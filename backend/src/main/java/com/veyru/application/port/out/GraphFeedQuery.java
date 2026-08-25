package com.veyru.application.port.out;

import com.veyru.application.discovery.GraphAffinity;
import com.veyru.application.discovery.GraphFeedItem;
import java.util.List;
import java.util.Map;

public interface GraphFeedQuery {
  List<GraphAffinity> getAuthorAffinities(String viewerId, List<String> authorIds);

  List<GraphFeedItem> getSuggestedUsers(String viewerId, int limit);

  Map<String, Long> getGraphStats();
}
