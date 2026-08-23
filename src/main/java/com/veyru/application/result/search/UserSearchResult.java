package com.veyru.application.result.search;

public class UserSearchResult {
  private String id;
  private String username;
  private String firstName;
  private String lastName;
  private String imageUrl;
  private String bio;
  private long followersCount;
  private boolean isFollowedByCurrentUser;
  private double searchScore;

  public UserSearchResult() {}

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

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof UserSearchResult)) return false;
    final UserSearchResult other = (UserSearchResult) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.getFollowersCount() != other.getFollowersCount()) return false;
    if (this.isFollowedByCurrentUser() != other.isFollowedByCurrentUser()) return false;
    if (Double.compare(this.getSearchScore(), other.getSearchScore()) != 0) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$username = this.getUsername();
    final Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final Object this$firstName = this.getFirstName();
    final Object other$firstName = other.getFirstName();
    if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName))
      return false;
    final Object this$lastName = this.getLastName();
    final Object other$lastName = other.getLastName();
    if (this$lastName == null ? other$lastName != null : !this$lastName.equals(other$lastName))
      return false;
    final Object this$imageUrl = this.getImageUrl();
    final Object other$imageUrl = other.getImageUrl();
    if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
      return false;
    final Object this$bio = this.getBio();
    final Object other$bio = other.getBio();
    if (this$bio == null ? other$bio != null : !this$bio.equals(other$bio)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof UserSearchResult;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $followersCount = this.getFollowersCount();
    result = result * PRIME + (int) ($followersCount >>> 32 ^ $followersCount);
    result = result * PRIME + (this.isFollowedByCurrentUser() ? 79 : 97);
    final long $searchScore = Double.doubleToLongBits(this.getSearchScore());
    result = result * PRIME + (int) ($searchScore >>> 32 ^ $searchScore);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final Object $firstName = this.getFirstName();
    result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
    final Object $lastName = this.getLastName();
    result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
    final Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final Object $bio = this.getBio();
    result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "UserSearchResult(id="
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
