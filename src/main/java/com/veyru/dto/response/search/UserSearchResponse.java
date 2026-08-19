package com.veyru.dto.response.search;

public class UserSearchResponse {
  private String id;
  private String username;
  private String firstName;
  private String lastName;
  private String imageUrl;
  private String bio;
  private long followersCount;
  private boolean isFollowedByCurrentUser;
  private double searchScore;

  public UserSearchResponse() {}

  public String getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public String getBio() {
    return this.bio;
  }

  public long getFollowersCount() {
    return this.followersCount;
  }

  public boolean isFollowedByCurrentUser() {
    return this.isFollowedByCurrentUser;
  }

  public double getSearchScore() {
    return this.searchScore;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public void setImageUrl(final String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setBio(final String bio) {
    this.bio = bio;
  }

  public void setFollowersCount(final long followersCount) {
    this.followersCount = followersCount;
  }

  public void setFollowedByCurrentUser(final boolean isFollowedByCurrentUser) {
    this.isFollowedByCurrentUser = isFollowedByCurrentUser;
  }

  public void setSearchScore(final double searchScore) {
    this.searchScore = searchScore;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof UserSearchResponse)) return false;
    final UserSearchResponse other = (UserSearchResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.getFollowersCount() != other.getFollowersCount()) return false;
    if (this.isFollowedByCurrentUser() != other.isFollowedByCurrentUser()) return false;
    if (java.lang.Double.compare(this.getSearchScore(), other.getSearchScore()) != 0) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$username = this.getUsername();
    final java.lang.Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final java.lang.Object this$firstName = this.getFirstName();
    final java.lang.Object other$firstName = other.getFirstName();
    if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName))
      return false;
    final java.lang.Object this$lastName = this.getLastName();
    final java.lang.Object other$lastName = other.getLastName();
    if (this$lastName == null ? other$lastName != null : !this$lastName.equals(other$lastName))
      return false;
    final java.lang.Object this$imageUrl = this.getImageUrl();
    final java.lang.Object other$imageUrl = other.getImageUrl();
    if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
      return false;
    final java.lang.Object this$bio = this.getBio();
    final java.lang.Object other$bio = other.getBio();
    if (this$bio == null ? other$bio != null : !this$bio.equals(other$bio)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof UserSearchResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $followersCount = this.getFollowersCount();
    result = result * PRIME + (int) ($followersCount >>> 32 ^ $followersCount);
    result = result * PRIME + (this.isFollowedByCurrentUser() ? 79 : 97);
    final long $searchScore = java.lang.Double.doubleToLongBits(this.getSearchScore());
    result = result * PRIME + (int) ($searchScore >>> 32 ^ $searchScore);
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $firstName = this.getFirstName();
    result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
    final java.lang.Object $lastName = this.getLastName();
    result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
    final java.lang.Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final java.lang.Object $bio = this.getBio();
    result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "UserSearchResponse(id="
        + this.getId()
        + ", username="
        + this.getUsername()
        + ", firstName="
        + this.getFirstName()
        + ", lastName="
        + this.getLastName()
        + ", imageUrl="
        + this.getImageUrl()
        + ", bio="
        + this.getBio()
        + ", followersCount="
        + this.getFollowersCount()
        + ", isFollowedByCurrentUser="
        + this.isFollowedByCurrentUser()
        + ", searchScore="
        + this.getSearchScore()
        + ")";
  }
}
