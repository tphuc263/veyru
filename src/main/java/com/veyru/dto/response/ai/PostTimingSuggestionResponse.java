package com.veyru.dto.response.ai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostTimingSuggestionResponse {
  private List<TimingSlot> bestTimes;
  private String aiSummary;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TimingSlot {
    private String dayOfWeek;
    private String timeRange;
    private double score;
    private String reason;
  }
}
