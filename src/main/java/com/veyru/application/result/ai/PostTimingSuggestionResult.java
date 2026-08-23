package com.veyru.application.result.ai;

import java.util.List;

public record PostTimingSuggestionResult(List<TimingSlot> bestTimes, String aiSummary) {
  public PostTimingSuggestionResult {
    bestTimes = bestTimes == null ? null : List.copyOf(bestTimes);
  }

  public record TimingSlot(String dayOfWeek, String timeRange, double score, String reason) {
    public String getDayOfWeek() {
      return dayOfWeek;
    }

    public String getTimeRange() {
      return timeRange;
    }

    public double getScore() {
      return score;
    }

    public String getReason() {
      return reason;
    }
  }

  public List<TimingSlot> getBestTimes() {
    return bestTimes;
  }

  public String getAiSummary() {
    return aiSummary;
  }
}
