package com.veyru.adapter.in.dto.request.ai;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record EngagementAnalysisRequest(@Min(0) @Max(10_000) int recentPostCount) {
  public int getRecentPostCount() {
    return recentPostCount;
  }
}
