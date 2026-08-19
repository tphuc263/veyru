package com.veyru.dto.response.user;

import java.util.HashMap;

public class UserProfileResponse {
  private String id;
  private String username;
  private String imageUrl;
  private HashMap<String, Long> stats;
  private String bio;
  private boolean isFollowingByCurrentUser;

  public UserProfileResponse() {}

  public String getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public HashMap<String, Long> getStats() {
    return this.stats;
  }

  public String getBio() {
    return this.bio;
  }

  public boolean isFollowingByCurrentUser() {
    return this.isFollowingByCurrentUser;
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

  public void setStats(final HashMap<String, Long> stats) {
    this.stats = stats;
  }

  public void setBio(final String bio) {
    this.bio = bio;
  }

  public void setFollowingByCurrentUser(final boolean isFollowingByCurrentUser) {
    this.isFollowingByCurrentUser = isFollowingByCurrentUser;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof UserProfileResponse)) return false;
    final UserProfileResponse other = (UserProfileResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.isFollowingByCurrentUser() != other.isFollowingByCurrentUser()) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$username = this.getUsername();
    final java.lang.Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final java.lang.Object this$imageUrl = this.getImageUrl();
    final java.lang.Object other$imageUrl = other.getImageUrl();
    if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
      return false;
    final java.lang.Object this$stats = this.getStats();
    final java.lang.Object other$stats = other.getStats();
    if (this$stats == null ? other$stats != null : !this$stats.equals(other$stats)) return false;
    final java.lang.Object this$bio = this.getBio();
    final java.lang.Object other$bio = other.getBio();
    if (this$bio == null ? other$bio != null : !this$bio.equals(other$bio)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof UserProfileResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isFollowingByCurrentUser() ? 79 : 97);
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final java.lang.Object $stats = this.getStats();
    result = result * PRIME + ($stats == null ? 43 : $stats.hashCode());
    final java.lang.Object $bio = this.getBio();
    result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "UserProfileResponse(id="
        + this.getId()
        + ", username="
        + this.getUsername()
        + ", imageUrl="
        + this.getImageUrl()
        + ", stats="
        + this.getStats()
        + ", bio="
        + this.getBio()
        + ", isFollowingByCurrentUser="
        + this.isFollowingByCurrentUser()
        + ")";
  }
}
