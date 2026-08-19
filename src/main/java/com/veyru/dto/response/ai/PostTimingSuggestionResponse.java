package com.veyru.dto.response.ai;

import java.util.List;

public class PostTimingSuggestionResponse {
  private List<TimingSlot> bestTimes;
  private String aiSummary;

  public static class TimingSlot {
    private String dayOfWeek;
    private String timeRange;
    private double score;
    private String reason;

    public String getDayOfWeek() {
      return this.dayOfWeek;
    }

    public String getTimeRange() {
      return this.timeRange;
    }

    public double getScore() {
      return this.score;
    }

    public String getReason() {
      return this.reason;
    }

    public void setDayOfWeek(final String dayOfWeek) {
      this.dayOfWeek = dayOfWeek;
    }

    public void setTimeRange(final String timeRange) {
      this.timeRange = timeRange;
    }

    public void setScore(final double score) {
      this.score = score;
    }

    public void setReason(final String reason) {
      this.reason = reason;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
      if (o == this) return true;
      if (!(o instanceof PostTimingSuggestionResponse.TimingSlot)) return false;
      final PostTimingSuggestionResponse.TimingSlot other =
          (PostTimingSuggestionResponse.TimingSlot) o;
      if (!other.canEqual((java.lang.Object) this)) return false;
      if (java.lang.Double.compare(this.getScore(), other.getScore()) != 0) return false;
      final java.lang.Object this$dayOfWeek = this.getDayOfWeek();
      final java.lang.Object other$dayOfWeek = other.getDayOfWeek();
      if (this$dayOfWeek == null
          ? other$dayOfWeek != null
          : !this$dayOfWeek.equals(other$dayOfWeek)) return false;
      final java.lang.Object this$timeRange = this.getTimeRange();
      final java.lang.Object other$timeRange = other.getTimeRange();
      if (this$timeRange == null
          ? other$timeRange != null
          : !this$timeRange.equals(other$timeRange)) return false;
      final java.lang.Object this$reason = this.getReason();
      final java.lang.Object other$reason = other.getReason();
      if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason))
        return false;
      return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
      return other instanceof PostTimingSuggestionResponse.TimingSlot;
    }

    @java.lang.Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final long $score = java.lang.Double.doubleToLongBits(this.getScore());
      result = result * PRIME + (int) ($score >>> 32 ^ $score);
      final java.lang.Object $dayOfWeek = this.getDayOfWeek();
      result = result * PRIME + ($dayOfWeek == null ? 43 : $dayOfWeek.hashCode());
      final java.lang.Object $timeRange = this.getTimeRange();
      result = result * PRIME + ($timeRange == null ? 43 : $timeRange.hashCode());
      final java.lang.Object $reason = this.getReason();
      result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
      return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
      return "PostTimingSuggestionResponse.TimingSlot(dayOfWeek="
          + this.getDayOfWeek()
          + ", timeRange="
          + this.getTimeRange()
          + ", score="
          + this.getScore()
          + ", reason="
          + this.getReason()
          + ")";
    }

    public TimingSlot() {}

    public TimingSlot(
        final String dayOfWeek, final String timeRange, final double score, final String reason) {
      this.dayOfWeek = dayOfWeek;
      this.timeRange = timeRange;
      this.score = score;
      this.reason = reason;
    }
  }

  public List<TimingSlot> getBestTimes() {
    return this.bestTimes;
  }

  public String getAiSummary() {
    return this.aiSummary;
  }

  public void setBestTimes(final List<TimingSlot> bestTimes) {
    this.bestTimes = bestTimes;
  }

  public void setAiSummary(final String aiSummary) {
    this.aiSummary = aiSummary;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof PostTimingSuggestionResponse)) return false;
    final PostTimingSuggestionResponse other = (PostTimingSuggestionResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$bestTimes = this.getBestTimes();
    final java.lang.Object other$bestTimes = other.getBestTimes();
    if (this$bestTimes == null ? other$bestTimes != null : !this$bestTimes.equals(other$bestTimes))
      return false;
    final java.lang.Object this$aiSummary = this.getAiSummary();
    final java.lang.Object other$aiSummary = other.getAiSummary();
    if (this$aiSummary == null ? other$aiSummary != null : !this$aiSummary.equals(other$aiSummary))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof PostTimingSuggestionResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $bestTimes = this.getBestTimes();
    result = result * PRIME + ($bestTimes == null ? 43 : $bestTimes.hashCode());
    final java.lang.Object $aiSummary = this.getAiSummary();
    result = result * PRIME + ($aiSummary == null ? 43 : $aiSummary.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "PostTimingSuggestionResponse(bestTimes="
        + this.getBestTimes()
        + ", aiSummary="
        + this.getAiSummary()
        + ")";
  }

  public PostTimingSuggestionResponse() {}

  public PostTimingSuggestionResponse(final List<TimingSlot> bestTimes, final String aiSummary) {
    this.bestTimes = bestTimes;
    this.aiSummary = aiSummary;
  }
}
