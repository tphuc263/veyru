package com.veyru.adapter.in.dto.response.follow;

public class FollowResponse {
  private String id;
  private String userId;
  private String username;
  private String userImageUrl;
  private String firstName;
  private String lastName;
  private String bio;
  private boolean isFollowedByCurrentUser;

  public FollowResponse() {}

  public String getId() {
    return this.id;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getUsername() {
    return this.username;
  }

  public String getUserImageUrl() {
    return this.userImageUrl;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public String getBio() {
    return this.bio;
  }

  public boolean isFollowedByCurrentUser() {
    return this.isFollowedByCurrentUser;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setUserImageUrl(final String userImageUrl) {
    this.userImageUrl = userImageUrl;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public void setBio(final String bio) {
    this.bio = bio;
  }

  public void setFollowedByCurrentUser(final boolean isFollowedByCurrentUser) {
    this.isFollowedByCurrentUser = isFollowedByCurrentUser;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof FollowResponse)) return false;
    final FollowResponse other = (FollowResponse) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.isFollowedByCurrentUser() != other.isFollowedByCurrentUser()) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$userId = this.getUserId();
    final Object other$userId = other.getUserId();
    if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
      return false;
    final Object this$username = this.getUsername();
    final Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final Object this$userImageUrl = this.getUserImageUrl();
    final Object other$userImageUrl = other.getUserImageUrl();
    if (this$userImageUrl == null
        ? other$userImageUrl != null
        : !this$userImageUrl.equals(other$userImageUrl)) return false;
    final Object this$firstName = this.getFirstName();
    final Object other$firstName = other.getFirstName();
    if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName))
      return false;
    final Object this$lastName = this.getLastName();
    final Object other$lastName = other.getLastName();
    if (this$lastName == null ? other$lastName != null : !this$lastName.equals(other$lastName))
      return false;
    final Object this$bio = this.getBio();
    final Object other$bio = other.getBio();
    if (this$bio == null ? other$bio != null : !this$bio.equals(other$bio)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof FollowResponse;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isFollowedByCurrentUser() ? 79 : 97);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final Object $firstName = this.getFirstName();
    result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
    final Object $lastName = this.getLastName();
    result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
    final Object $bio = this.getBio();
    result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "FollowResponse(id="
        + this.getId()
        + ", userId="
        + this.getUserId()
        + ", username="
        + this.getUsername()
        + ", userImageUrl="
        + this.getUserImageUrl()
        + ", firstName="
        + this.getFirstName()
        + ", lastName="
        + this.getLastName()
        + ", bio="
        + this.getBio()
        + ", isFollowedByCurrentUser="
        + this.isFollowedByCurrentUser()
        + ")";
  }
}
