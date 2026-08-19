package com.veyru.model;

import com.veyru.enums.UserRole;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
  @Id private String id;

  @Indexed(unique = true)
  private String username;

  @Indexed(unique = true)
  private String email;

  @Indexed(unique = true, sparse = true)
  private String phoneNumber;

  private String password;
  private UserRole role;
  private String imageUrl;
  private String bio;
  private Instant createdAt;
  private long photoCount;
  private long followerCount;
  private long followingCount;
  private String resetToken;
  private Instant resetTokenExpiry;

  public boolean isResetTokenValid() {
    return resetToken != null
        && resetTokenExpiry != null
        && Instant.now().isBefore(resetTokenExpiry);
  }

  public String getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getEmail() {
    return this.email;
  }

  public String getPhoneNumber() {
    return this.phoneNumber;
  }

  public String getPassword() {
    return this.password;
  }

  public UserRole getRole() {
    return this.role;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public String getBio() {
    return this.bio;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public long getPhotoCount() {
    return this.photoCount;
  }

  public long getFollowerCount() {
    return this.followerCount;
  }

  public long getFollowingCount() {
    return this.followingCount;
  }

  public String getResetToken() {
    return this.resetToken;
  }

  public Instant getResetTokenExpiry() {
    return this.resetTokenExpiry;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public void setPhoneNumber(final String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public void setRole(final UserRole role) {
    this.role = role;
  }

  public void setImageUrl(final String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setBio(final String bio) {
    this.bio = bio;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setPhotoCount(final long photoCount) {
    this.photoCount = photoCount;
  }

  public void setFollowerCount(final long followerCount) {
    this.followerCount = followerCount;
  }

  public void setFollowingCount(final long followingCount) {
    this.followingCount = followingCount;
  }

  public void setResetToken(final String resetToken) {
    this.resetToken = resetToken;
  }

  public void setResetTokenExpiry(final Instant resetTokenExpiry) {
    this.resetTokenExpiry = resetTokenExpiry;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof User)) return false;
    final User other = (User) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.getPhotoCount() != other.getPhotoCount()) return false;
    if (this.getFollowerCount() != other.getFollowerCount()) return false;
    if (this.getFollowingCount() != other.getFollowingCount()) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$username = this.getUsername();
    final java.lang.Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final java.lang.Object this$email = this.getEmail();
    final java.lang.Object other$email = other.getEmail();
    if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
    final java.lang.Object this$phoneNumber = this.getPhoneNumber();
    final java.lang.Object other$phoneNumber = other.getPhoneNumber();
    if (this$phoneNumber == null
        ? other$phoneNumber != null
        : !this$phoneNumber.equals(other$phoneNumber)) return false;
    final java.lang.Object this$password = this.getPassword();
    final java.lang.Object other$password = other.getPassword();
    if (this$password == null ? other$password != null : !this$password.equals(other$password))
      return false;
    final java.lang.Object this$role = this.getRole();
    final java.lang.Object other$role = other.getRole();
    if (this$role == null ? other$role != null : !this$role.equals(other$role)) return false;
    final java.lang.Object this$imageUrl = this.getImageUrl();
    final java.lang.Object other$imageUrl = other.getImageUrl();
    if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
      return false;
    final java.lang.Object this$bio = this.getBio();
    final java.lang.Object other$bio = other.getBio();
    if (this$bio == null ? other$bio != null : !this$bio.equals(other$bio)) return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final java.lang.Object this$resetToken = this.getResetToken();
    final java.lang.Object other$resetToken = other.getResetToken();
    if (this$resetToken == null
        ? other$resetToken != null
        : !this$resetToken.equals(other$resetToken)) return false;
    final java.lang.Object this$resetTokenExpiry = this.getResetTokenExpiry();
    final java.lang.Object other$resetTokenExpiry = other.getResetTokenExpiry();
    if (this$resetTokenExpiry == null
        ? other$resetTokenExpiry != null
        : !this$resetTokenExpiry.equals(other$resetTokenExpiry)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof User;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $photoCount = this.getPhotoCount();
    result = result * PRIME + (int) ($photoCount >>> 32 ^ $photoCount);
    final long $followerCount = this.getFollowerCount();
    result = result * PRIME + (int) ($followerCount >>> 32 ^ $followerCount);
    final long $followingCount = this.getFollowingCount();
    result = result * PRIME + (int) ($followingCount >>> 32 ^ $followingCount);
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $email = this.getEmail();
    result = result * PRIME + ($email == null ? 43 : $email.hashCode());
    final java.lang.Object $phoneNumber = this.getPhoneNumber();
    result = result * PRIME + ($phoneNumber == null ? 43 : $phoneNumber.hashCode());
    final java.lang.Object $password = this.getPassword();
    result = result * PRIME + ($password == null ? 43 : $password.hashCode());
    final java.lang.Object $role = this.getRole();
    result = result * PRIME + ($role == null ? 43 : $role.hashCode());
    final java.lang.Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final java.lang.Object $bio = this.getBio();
    result = result * PRIME + ($bio == null ? 43 : $bio.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final java.lang.Object $resetToken = this.getResetToken();
    result = result * PRIME + ($resetToken == null ? 43 : $resetToken.hashCode());
    final java.lang.Object $resetTokenExpiry = this.getResetTokenExpiry();
    result = result * PRIME + ($resetTokenExpiry == null ? 43 : $resetTokenExpiry.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "User(id="
        + this.getId()
        + ", username="
        + this.getUsername()
        + ", email="
        + this.getEmail()
        + ", phoneNumber="
        + this.getPhoneNumber()
        + ", password="
        + this.getPassword()
        + ", role="
        + this.getRole()
        + ", imageUrl="
        + this.getImageUrl()
        + ", bio="
        + this.getBio()
        + ", createdAt="
        + this.getCreatedAt()
        + ", photoCount="
        + this.getPhotoCount()
        + ", followerCount="
        + this.getFollowerCount()
        + ", followingCount="
        + this.getFollowingCount()
        + ", resetToken="
        + this.getResetToken()
        + ", resetTokenExpiry="
        + this.getResetTokenExpiry()
        + ")";
  }

  public User() {}

  public User(
      final String id,
      final String username,
      final String email,
      final String phoneNumber,
      final String password,
      final UserRole role,
      final String imageUrl,
      final String bio,
      final Instant createdAt,
      final long photoCount,
      final long followerCount,
      final long followingCount,
      final String resetToken,
      final Instant resetTokenExpiry) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.password = password;
    this.role = role;
    this.imageUrl = imageUrl;
    this.bio = bio;
    this.createdAt = createdAt;
    this.photoCount = photoCount;
    this.followerCount = followerCount;
    this.followingCount = followingCount;
    this.resetToken = resetToken;
    this.resetTokenExpiry = resetTokenExpiry;
  }
}
