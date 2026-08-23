package com.veyru.application.result.ai;

import java.util.List;

public class PostTimingSuggestionResult {
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

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof PostTimingSuggestionResult.TimingSlot)) return false;
      final PostTimingSuggestionResult.TimingSlot other =
          (PostTimingSuggestionResult.TimingSlot) o;
      if (!other.canEqual((Object) this)) return false;
      if (Double.compare(this.getScore(), other.getScore()) != 0) return false;
      final Object this$dayOfWeek = this.getDayOfWeek();
      final Object other$dayOfWeek = other.getDayOfWeek();
      if (this$dayOfWeek == null
          ? other$dayOfWeek != null
          : !this$dayOfWeek.equals(other$dayOfWeek)) return false;
      final Object this$timeRange = this.getTimeRange();
      final Object other$timeRange = other.getTimeRange();
      if (this$timeRange == null
          ? other$timeRange != null
          : !this$timeRange.equals(other$timeRange)) return false;
      final Object this$reason = this.getReason();
      final Object other$reason = other.getReason();
      if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason))
        return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof PostTimingSuggestionResult.TimingSlot;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final long $score = Double.doubleToLongBits(this.getScore());
      result = result * PRIME + (int) ($score >>> 32 ^ $score);
      final Object $dayOfWeek = this.getDayOfWeek();
      result = result * PRIME + ($dayOfWeek == null ? 43 : $dayOfWeek.hashCode());
      final Object $timeRange = this.getTimeRange();
      result = result * PRIME + ($timeRange == null ? 43 : $timeRange.hashCode());
      final Object $reason = this.getReason();
      result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "PostTimingSuggestionResult.TimingSlot(dayOfWeek="
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

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof PostTimingSuggestionResult)) return false;
    final PostTimingSuggestionResult other = (PostTimingSuggestionResult) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$bestTimes = this.getBestTimes();
    final Object other$bestTimes = other.getBestTimes();
    if (this$bestTimes == null ? other$bestTimes != null : !this$bestTimes.equals(other$bestTimes))
      return false;
    final Object this$aiSummary = this.getAiSummary();
    final Object other$aiSummary = other.getAiSummary();
    if (this$aiSummary == null ? other$aiSummary != null : !this$aiSummary.equals(other$aiSummary))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof PostTimingSuggestionResult;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $bestTimes = this.getBestTimes();
    result = result * PRIME + ($bestTimes == null ? 43 : $bestTimes.hashCode());
    final Object $aiSummary = this.getAiSummary();
    result = result * PRIME + ($aiSummary == null ? 43 : $aiSummary.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "PostTimingSuggestionResult(bestTimes="
        + this.getBestTimes()
        + ", aiSummary="
        + this.getAiSummary()
        + ")";
  }

  public PostTimingSuggestionResult() {}

  public PostTimingSuggestionResult(final List<TimingSlot> bestTimes, final String aiSummary) {
    this.bestTimes = bestTimes;
    this.aiSummary = aiSummary;
  }
}
