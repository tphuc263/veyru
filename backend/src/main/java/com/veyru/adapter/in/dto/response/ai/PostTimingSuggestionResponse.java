package com.veyru.adapter.in.dto.response.ai;

import com.veyru.application.result.ai.PostTimingSuggestionResult;
import java.util.List;

public record PostTimingSuggestionResponse(List<TimingSlot> bestTimes, String aiSummary) {
  public record TimingSlot(String dayOfWeek, String timeRange, double score, String reason) {
    static TimingSlot from(PostTimingSuggestionResult.TimingSlot value) {
      return new TimingSlot(
          value.getDayOfWeek(), value.getTimeRange(), value.getScore(), value.getReason());
    }
  }

  public static PostTimingSuggestionResponse from(PostTimingSuggestionResult value) {
    return new PostTimingSuggestionResponse(
        value.getBestTimes().stream().map(TimingSlot::from).toList(), value.getAiSummary());
  }
}
