package com.veyru.dto.response.ai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngagementAnalysisResponse {
  private double averageLikes;
  private double averageComments;
  private double engagementRate;
  private String trend;
  private List<PostInsight> topPosts;
  private String aiSummary;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostInsight {
    private String photoId;
    private String caption;
    private String imageUrl;
    private long likeCount;
    private long commentCount;
    private double engagementScore;
  }
}
