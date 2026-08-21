package com.veyru.adapter.in.dto.request.ai;

public record EngagementAnalysisRequest(int recentPostCount) {
  public int getRecentPostCount() {
    return recentPostCount;
  }
}
