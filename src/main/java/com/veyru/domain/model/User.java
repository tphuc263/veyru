package com.veyru.domain.model;

import com.veyru.domain.enums.UserRole;
import java.time.Instant;

public class User {
  private String id;
  private String username;
  private String email;
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

  public static User registered(
      String username, String email, String encodedPassword, Instant createdAt) {
    User user = new User();
    user.username = username;
    user.email = email;
    user.password = encodedPassword;
    user.role = UserRole.ROLE_USER;
    user.createdAt = createdAt;
    return user;
  }

  public static User oauthRegistered(
      String username,
      String email,
      String encodedPassword,
      String imageUrl,
      String bio,
      Instant createdAt) {
    User user = registered(username, email, encodedPassword, createdAt);
    user.imageUrl = imageUrl;
    user.bio = bio;
    return user;
  }

  public User updateProfile(String username, String bio, String imageUrl) {
    if (username != null) this.username = username;
    if (bio != null) this.bio = bio;
    if (imageUrl != null) this.imageUrl = imageUrl;
    return this;
  }

  public User requestPasswordReset(String token, Instant expiry) {
    this.resetToken = token;
    this.resetTokenExpiry = expiry;
    return this;
  }

  public User resetPassword(String encodedPassword) {
    this.password = encodedPassword;
    this.resetToken = null;
    this.resetTokenExpiry = null;
    return this;
  }

  public boolean hasValidResetToken(Instant now) {
    return resetToken != null && resetTokenExpiry != null && now.isBefore(resetTokenExpiry);
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















  @Override
  public String toString() {
    return "User(id=" + id + ", username=" + username + ", email=" + email + ")";
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
