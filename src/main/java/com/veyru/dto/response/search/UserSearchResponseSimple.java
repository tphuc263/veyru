package com.veyru.dto.response.search;

public class UserSearchResponseSimple {
  private String id;
  private String username;
  private String imageUrl;

  public UserSearchResponseSimple() {}

  public String getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getImageUrl() {
    return this.imageUrl;
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

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof UserSearchResponseSimple)) return false;
    final UserSearchResponseSimple other = (UserSearchResponseSimple) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
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
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof UserSearchResponseSimple;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "UserSearchResponseSimple(id="
        + this.getId()
        + ", username="
        + this.getUsername()
        + ", imageUrl="
        + this.getImageUrl()
        + ")";
  }
}
