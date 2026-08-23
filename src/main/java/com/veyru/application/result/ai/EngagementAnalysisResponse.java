package com.veyru.application.result.ai;

import java.util.List;

public class EngagementAnalysisResponse {
  private double averageLikes;
  private double averageComments;
  private double engagementRate;
  private String trend;
  private List<PostInsight> topPosts;
  private String aiSummary;

  public static class PostInsight {
    private String photoId;
    private String caption;
    private String imageUrl;
    private long likeCount;
    private long commentCount;
    private double engagementScore;

    public String getPhotoId() {
      return this.photoId;
    }

    public String getCaption() {
      return this.caption;
    }

    public String getImageUrl() {
      return this.imageUrl;
    }

    public long getLikeCount() {
      return this.likeCount;
    }

    public long getCommentCount() {
      return this.commentCount;
    }

    public double getEngagementScore() {
      return this.engagementScore;
    }

    public void setPhotoId(final String photoId) {
      this.photoId = photoId;
    }

    public void setCaption(final String caption) {
      this.caption = caption;
    }

    public void setImageUrl(final String imageUrl) {
      this.imageUrl = imageUrl;
    }

    public void setLikeCount(final long likeCount) {
      this.likeCount = likeCount;
    }

    public void setCommentCount(final long commentCount) {
      this.commentCount = commentCount;
    }

    public void setEngagementScore(final double engagementScore) {
      this.engagementScore = engagementScore;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof EngagementAnalysisResponse.PostInsight)) return false;
      final EngagementAnalysisResponse.PostInsight other =
          (EngagementAnalysisResponse.PostInsight) o;
      if (!other.canEqual((Object) this)) return false;
      if (this.getLikeCount() != other.getLikeCount()) return false;
      if (this.getCommentCount() != other.getCommentCount()) return false;
      if (Double.compare(this.getEngagementScore(), other.getEngagementScore()) != 0) return false;
      final Object this$photoId = this.getPhotoId();
      final Object other$photoId = other.getPhotoId();
      if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
        return false;
      final Object this$caption = this.getCaption();
      final Object other$caption = other.getCaption();
      if (this$caption == null ? other$caption != null : !this$caption.equals(other$caption))
        return false;
      final Object this$imageUrl = this.getImageUrl();
      final Object other$imageUrl = other.getImageUrl();
      if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
        return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof EngagementAnalysisResponse.PostInsight;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final long $likeCount = this.getLikeCount();
      result = result * PRIME + (int) ($likeCount >>> 32 ^ $likeCount);
      final long $commentCount = this.getCommentCount();
      result = result * PRIME + (int) ($commentCount >>> 32 ^ $commentCount);
      final long $engagementScore = Double.doubleToLongBits(this.getEngagementScore());
      result = result * PRIME + (int) ($engagementScore >>> 32 ^ $engagementScore);
      final Object $photoId = this.getPhotoId();
      result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
      final Object $caption = this.getCaption();
      result = result * PRIME + ($caption == null ? 43 : $caption.hashCode());
      final Object $imageUrl = this.getImageUrl();
      result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "EngagementAnalysisResponse.PostInsight(photoId="
          + this.getPhotoId()
          + ", caption="
          + this.getCaption()
          + ", imageUrl="
          + this.getImageUrl()
          + ", likeCount="
          + this.getLikeCount()
          + ", commentCount="
          + this.getCommentCount()
          + ", engagementScore="
          + this.getEngagementScore()
          + ")";
    }

    public PostInsight() {}

    public PostInsight(
        final String photoId,
        final String caption,
        final String imageUrl,
        final long likeCount,
        final long commentCount,
        final double engagementScore) {
      this.photoId = photoId;
      this.caption = caption;
      this.imageUrl = imageUrl;
      this.likeCount = likeCount;
      this.commentCount = commentCount;
      this.engagementScore = engagementScore;
    }
  }

  public double getAverageLikes() {
    return this.averageLikes;
  }

  public double getAverageComments() {
    return this.averageComments;
  }

  public double getEngagementRate() {
    return this.engagementRate;
  }

  public String getTrend() {
    return this.trend;
  }

  public List<PostInsight> getTopPosts() {
    return this.topPosts;
  }

  public String getAiSummary() {
    return this.aiSummary;
  }

  public void setAverageLikes(final double averageLikes) {
    this.averageLikes = averageLikes;
  }

  public void setAverageComments(final double averageComments) {
    this.averageComments = averageComments;
  }

  public void setEngagementRate(final double engagementRate) {
    this.engagementRate = engagementRate;
  }

  public void setTrend(final String trend) {
    this.trend = trend;
  }

  public void setTopPosts(final List<PostInsight> topPosts) {
    this.topPosts = topPosts;
  }

  public void setAiSummary(final String aiSummary) {
    this.aiSummary = aiSummary;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof EngagementAnalysisResponse)) return false;
    final EngagementAnalysisResponse other = (EngagementAnalysisResponse) o;
    if (!other.canEqual((Object) this)) return false;
    if (Double.compare(this.getAverageLikes(), other.getAverageLikes()) != 0) return false;
    if (Double.compare(this.getAverageComments(), other.getAverageComments()) != 0) return false;
    if (Double.compare(this.getEngagementRate(), other.getEngagementRate()) != 0) return false;
    final Object this$trend = this.getTrend();
    final Object other$trend = other.getTrend();
    if (this$trend == null ? other$trend != null : !this$trend.equals(other$trend)) return false;
    final Object this$topPosts = this.getTopPosts();
    final Object other$topPosts = other.getTopPosts();
    if (this$topPosts == null ? other$topPosts != null : !this$topPosts.equals(other$topPosts))
      return false;
    final Object this$aiSummary = this.getAiSummary();
    final Object other$aiSummary = other.getAiSummary();
    if (this$aiSummary == null ? other$aiSummary != null : !this$aiSummary.equals(other$aiSummary))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof EngagementAnalysisResponse;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $averageLikes = Double.doubleToLongBits(this.getAverageLikes());
    result = result * PRIME + (int) ($averageLikes >>> 32 ^ $averageLikes);
    final long $averageComments = Double.doubleToLongBits(this.getAverageComments());
    result = result * PRIME + (int) ($averageComments >>> 32 ^ $averageComments);
    final long $engagementRate = Double.doubleToLongBits(this.getEngagementRate());
    result = result * PRIME + (int) ($engagementRate >>> 32 ^ $engagementRate);
    final Object $trend = this.getTrend();
    result = result * PRIME + ($trend == null ? 43 : $trend.hashCode());
    final Object $topPosts = this.getTopPosts();
    result = result * PRIME + ($topPosts == null ? 43 : $topPosts.hashCode());
    final Object $aiSummary = this.getAiSummary();
    result = result * PRIME + ($aiSummary == null ? 43 : $aiSummary.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "EngagementAnalysisResponse(averageLikes="
        + this.getAverageLikes()
        + ", averageComments="
        + this.getAverageComments()
        + ", engagementRate="
        + this.getEngagementRate()
        + ", trend="
        + this.getTrend()
        + ", topPosts="
        + this.getTopPosts()
        + ", aiSummary="
        + this.getAiSummary()
        + ")";
  }

  public EngagementAnalysisResponse() {}

  public EngagementAnalysisResponse(
      final double averageLikes,
      final double averageComments,
      final double engagementRate,
      final String trend,
      final List<PostInsight> topPosts,
      final String aiSummary) {
    this.averageLikes = averageLikes;
    this.averageComments = averageComments;
    this.engagementRate = engagementRate;
    this.trend = trend;
    this.topPosts = topPosts;
    this.aiSummary = aiSummary;
  }
}
