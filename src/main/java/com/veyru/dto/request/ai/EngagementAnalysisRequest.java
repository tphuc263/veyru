package com.veyru.dto.request.ai;

public record EngagementAnalysisRequest(int recentPostCount) {
  public int getRecentPostCount() {
    return recentPostCount;
  }
}
