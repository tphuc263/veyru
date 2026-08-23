package com.veyru.application.result.ai;

import java.util.List;

public record EngagementAnalysisResult(
    double averageLikes,
    double averageComments,
    double engagementRate,
    String trend,
    List<PostInsight> topPosts,
    String aiSummary) {
  public EngagementAnalysisResult {
    topPosts = topPosts == null ? null : List.copyOf(topPosts);
  }

  public record PostInsight(
      String photoId,
      String caption,
      String imageUrl,
      long likeCount,
      long commentCount,
      double engagementScore) {
    public String getPhotoId() { return photoId; }
    public String getCaption() { return caption; }
    public String getImageUrl() { return imageUrl; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }
    public double getEngagementScore() { return engagementScore; }
  }

  public double getAverageLikes() { return averageLikes; }
  public double getAverageComments() { return averageComments; }
  public double getEngagementRate() { return engagementRate; }
  public String getTrend() { return trend; }
  public List<PostInsight> getTopPosts() { return topPosts; }
  public String getAiSummary() { return aiSummary; }
}
