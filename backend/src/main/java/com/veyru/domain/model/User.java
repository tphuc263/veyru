package com.veyru.domain.model;

import com.veyru.domain.enums.UserRole;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable user aggregate identified by its persisted ID. Profile and credential transitions
 * preserve account invariants and return a new aggregate instance.
 */
public final class User {
  private final String id;
  private final String username;
  private final String email;
  private final String phoneNumber;
  private final String password;
  private final UserRole role;
  private final String imageUrl;
  private final String bio;
  private final Instant createdAt;
  private final long photoCount;
  private final long followerCount;
  private final long followingCount;
  private final String resetToken;
  private final Instant resetTokenExpiry;

  private User(
      String id,
      String username,
      String email,
      String phoneNumber,
      String password,
      UserRole role,
      String imageUrl,
      String bio,
      Instant createdAt,
      long photoCount,
      long followerCount,
      long followingCount,
      String resetToken,
      Instant resetTokenExpiry) {
    this.id = id == null ? null : DomainRules.required(id, "user.id.invalid", "User ID is invalid");
    this.username = validateUsername(username);
    this.email = DomainRules.email(email);
    this.phoneNumber = phoneNumber == null ? null : phoneNumber.trim();
    this.password =
        DomainRules.required(password, "user.password.required", "Encoded password is required");
    DomainRules.require(role != null, "user.role.required", "User role is required");
    this.role = role;
    this.imageUrl = imageUrl == null ? null : imageUrl.trim();
    this.bio = DomainRules.limited(bio, 500, "user.bio.too-long", "Bio is too long");
    DomainRules.require(createdAt != null, "user.time.required", "User creation time is required");
    this.createdAt = createdAt;
    this.photoCount =
        DomainRules.nonNegative(
            photoCount, "user.photo-count.negative", "Photo count cannot be negative");
    this.followerCount =
        DomainRules.nonNegative(
            followerCount, "user.follower-count.negative", "Follower count cannot be negative");
    this.followingCount =
        DomainRules.nonNegative(
            followingCount, "user.following-count.negative", "Following count cannot be negative");
    DomainRules.require(
        (resetToken == null) == (resetTokenExpiry == null),
        "user.reset-state.incomplete",
        "Reset token and expiry must be present together");
    this.resetToken =
        resetToken == null
            ? null
            : DomainRules.required(
                resetToken, "user.reset-token.required", "Reset token cannot be blank");
    this.resetTokenExpiry = resetTokenExpiry;
  }

  public static User registered(
      String username, String email, String encodedPassword, Instant createdAt) {
    return new User(
        null,
        username,
        email,
        null,
        encodedPassword,
        UserRole.ROLE_USER,
        null,
        null,
        createdAt,
        0,
        0,
        0,
        null,
        null);
  }

  public static User oauthRegistered(
      String username,
      String email,
      String encodedPassword,
      String imageUrl,
      String bio,
      Instant createdAt) {
    return new User(
        null,
        username,
        email,
        null,
        encodedPassword,
        UserRole.ROLE_USER,
        imageUrl,
        bio,
        createdAt,
        0,
        0,
        0,
        null,
        null);
  }

  /** Reconstitutes persisted state. New users should be created through a registration factory. */
  public static User restore(
      String id,
      String username,
      String email,
      String phoneNumber,
      String password,
      UserRole role,
      String imageUrl,
      String bio,
      Instant createdAt,
      long photoCount,
      long followerCount,
      long followingCount,
      String resetToken,
      Instant resetTokenExpiry) {
    return new User(
        DomainRules.required(id, "user.id.required", "Persisted user ID is required"),
        username,
        email,
        phoneNumber,
        password,
        role,
        imageUrl,
        bio,
        createdAt,
        photoCount,
        followerCount,
        followingCount,
        resetToken,
        resetTokenExpiry);
  }

  public User withUpdatedProfile(String username, String bio, String imageUrl) {
    return copy(
        username == null ? this.username : username,
        imageUrl == null ? this.imageUrl : imageUrl,
        bio == null ? this.bio : bio,
        password,
        resetToken,
        resetTokenExpiry);
  }

  public User withPasswordResetRequested(String token, Instant expiry) {
    DomainRules.require(expiry != null, "user.reset-expiry.required", "Reset expiry is required");
    return copy(username, imageUrl, bio, password, token, expiry);
  }

  public User withResetPassword(String encodedPassword) {
    return copy(username, imageUrl, bio, encodedPassword, null, null);
  }

  public boolean hasValidResetToken(Instant now) {
    return now != null
        && resetToken != null
        && resetTokenExpiry != null
        && now.isBefore(resetTokenExpiry);
  }

  private User copy(
      String username,
      String imageUrl,
      String bio,
      String password,
      String resetToken,
      Instant resetTokenExpiry) {
    return new User(
        id,
        username,
        email,
        phoneNumber,
        password,
        role,
        imageUrl,
        bio,
        createdAt,
        photoCount,
        followerCount,
        followingCount,
        resetToken,
        resetTokenExpiry);
  }

  private static String validateUsername(String username) {
    String normalized =
        DomainRules.required(username, "user.username.required", "Username is required");
    DomainRules.require(
        normalized.length() >= 3 && normalized.length() <= 30,
        "user.username.invalid-length",
        "Username must contain between 3 and 30 characters");
    return normalized;
  }

  public String id() {
    return id;
  }

  public String username() {
    return username;
  }

  public String email() {
    return email;
  }

  public String phoneNumber() {
    return phoneNumber;
  }

  public String password() {
    return password;
  }

  public UserRole role() {
    return role;
  }

  public String imageUrl() {
    return imageUrl;
  }

  public String bio() {
    return bio;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public long photoCount() {
    return photoCount;
  }

  public long followerCount() {
    return followerCount;
  }

  public long followingCount() {
    return followingCount;
  }

  public String resetToken() {
    return resetToken;
  }

  public Instant resetTokenExpiry() {
    return resetTokenExpiry;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    return other instanceof User user && id != null && id.equals(user.id);
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : Objects.hash(id);
  }

  @Override
  public String toString() {
    return "User[id=" + id + ", username=" + username + "]";
  }
}
