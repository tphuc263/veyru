package com.veyru.dto.response.follow;

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

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof FollowResponse)) return false;
    final FollowResponse other = (FollowResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.isFollowedByCurrentUser() != other.isFollowedByCurrentUser()) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$userId = this.getUserId();
    final java.lang.Object other$userId = other.getUserId();
    if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
      return false;
    final java.lang.Object this$username = this.getUsername();
    final java.lang.Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final java.lang.Object this$userImageUrl = this.getUserImageUrl();
    final java.lang.Object other$userImageUrl = other.getUserImageUrl();
    if (this$userImageUrl == null
        ? other$userImageUrl != null
        : !this$userImageUrl.equals(other$userImageUrl)) return false;
    final java.lang.Object this$firstName = this.getFirstName();
    final java.lang.Object other$firstName = other.getFirstName();
    if (this$firstName == null ? other$firstName != null : !this$firstName.equals(other$firstName))
      return false;
    final java.lang.Object this$lastName = this.getLastName();
    final java.lang.Object other$lastName = other.getLastName();
    if (this$lastName == null ? other$lastName != null : !this$lastName.equals(other$lastName))
      return false;
    final java.lang.Object this$bio = this.getBio();
    final java.lang.Object other$bio = other.getBio();
    if (this$bio == null ? other$bio != null : !this$bio.equals(other$bio)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof FollowResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isFollowedByCurrentUser() ? 79 : 97);
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final java.lang.Object $firstName = this.getFirstName();
    result = result * PRIME + ($firstName == null ? 43 : $firstName.hashCode());
    final java.lang.Object $lastName = this.getLastName();
    result = result * PRIME + ($lastName == null ? 43 : $lastName.hashCode());
    final java.lang.Object $bio = this.getBio();
    result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
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
