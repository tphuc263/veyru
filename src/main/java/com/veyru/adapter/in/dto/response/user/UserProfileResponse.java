package com.veyru.adapter.in.dto.response.user;

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

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof UserProfileResponse)) return false;
    final UserProfileResponse other = (UserProfileResponse) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.isFollowingByCurrentUser() != other.isFollowingByCurrentUser()) return false;
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
    final Object this$stats = this.getStats();
    final Object other$stats = other.getStats();
    if (this$stats == null ? other$stats != null : !this$stats.equals(other$stats)) return false;
    final Object this$bio = this.getBio();
    final Object other$bio = other.getBio();
    if (this$bio == null ? other$bio != null : !this$bio.equals(other$bio)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof UserProfileResponse;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isFollowingByCurrentUser() ? 79 : 97);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final Object $stats = this.getStats();
    result = result * PRIME + ($stats == null ? 43 : $stats.hashCode());
    final Object $bio = this.getBio();
    result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
    return result;
  }

  @Override
  public String toString() {
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
