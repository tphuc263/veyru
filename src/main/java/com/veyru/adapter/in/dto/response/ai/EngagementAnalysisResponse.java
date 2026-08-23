package com.veyru.adapter.in.dto.response.ai;

import com.veyru.application.result.ai.EngagementAnalysisResult;
import java.util.List;

public record EngagementAnalysisResponse(
    double averageLikes,
    double averageComments,
    double engagementRate,
    String trend,
    List<PostInsight> topPosts,
    String aiSummary) {
  public record PostInsight(
      String photoId,
      String caption,
      String imageUrl,
      long likeCount,
      long commentCount,
      double engagementScore) {
    static PostInsight from(EngagementAnalysisResult.PostInsight value) {
      return new PostInsight(
          value.getPhotoId(), value.getCaption(), value.getImageUrl(), value.getLikeCount(),
          value.getCommentCount(), value.getEngagementScore());
    }
  }

  public static EngagementAnalysisResponse from(EngagementAnalysisResult value) {
    return new EngagementAnalysisResponse(
        value.getAverageLikes(), value.getAverageComments(), value.getEngagementRate(),
        value.getTrend(), value.getTopPosts().stream().map(PostInsight::from).toList(),
        value.getAiSummary());
  }
}
