package com.veyru.adapter.in.dto.response.recommendation;

/** Response DTO for a recommended user ("Suggested for you"). */
public class RecommendedUserResponse {
  private String id;
  private String username;
  private String imageUrl;
  private String bio;
  private long followerCount;
  private long photoCount;
  private double similarityScore;
  private String reason; // e.g., "Similar interests in travel, photography"

  public RecommendedUserResponse() {}

  public String getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public String getBio() {
    return this.bio;
  }

  public long getFollowerCount() {
    return this.followerCount;
  }

  public long getPhotoCount() {
    return this.photoCount;
  }

  public double getSimilarityScore() {
    return this.similarityScore;
  }

  public String getReason() {
    return this.reason;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setImageUrl(final String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setBio(final String bio) {
    this.bio = bio;
  }

  public void setFollowerCount(final long followerCount) {
    this.followerCount = followerCount;
  }

  public void setPhotoCount(final long photoCount) {
    this.photoCount = photoCount;
  }

  public void setSimilarityScore(final double similarityScore) {
    this.similarityScore = similarityScore;
  }

  public void setReason(final String reason) {
    this.reason = reason;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof RecommendedUserResponse)) return false;
    final RecommendedUserResponse other = (RecommendedUserResponse) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.getFollowerCount() != other.getFollowerCount()) return false;
    if (this.getPhotoCount() != other.getPhotoCount()) return false;
    if (Double.compare(this.getSimilarityScore(), other.getSimilarityScore()) != 0) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$username = this.getUsername();
    final Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final Object this$imageUrl = this.getImageUrl();
    final Object other$imageUrl = other.getImageUrl();
    if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
      return false;
    final Object this$bio = this.getBio();
    final Object other$bio = other.getBio();
    if (this$bio == null ? other$bio != null : !this$bio.equals(other$bio)) return false;
    final Object this$reason = this.getReason();
    final Object other$reason = other.getReason();
    if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof RecommendedUserResponse;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $followerCount = this.getFollowerCount();
    result = result * PRIME + (int) ($followerCount >>> 32 ^ $followerCount);
    final long $photoCount = this.getPhotoCount();
    result = result * PRIME + (int) ($photoCount >>> 32 ^ $photoCount);
    final long $similarityScore = Double.doubleToLongBits(this.getSimilarityScore());
    result = result * PRIME + (int) ($similarityScore >>> 32 ^ $similarityScore);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final Object $bio = this.getBio();
    result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
    final Object $reason = this.getReason();
    result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "RecommendedUserResponse(id="
        + this.getId()
        + ", username="
        + this.getUsername()
        + ", imageUrl="
        + this.getImageUrl()
        + ", bio="
        + this.getBio()
        + ", followerCount="
        + this.getFollowerCount()
        + ", photoCount="
        + this.getPhotoCount()
        + ", similarityScore="
        + this.getSimilarityScore()
        + ", reason="
        + this.getReason()
        + ")";
  }
}
