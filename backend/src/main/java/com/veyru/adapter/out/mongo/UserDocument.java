package com.veyru.adapter.out.mongo;

import com.veyru.domain.enums.UserRole;
import com.veyru.domain.model.User;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Mongo representation of a user. Domain construction remains owned by {@link User}. */
@Document(collection = "users")
final class UserDocument {
  @Id private String id;
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

  static UserDocument fromDomain(User user) {
    UserDocument document = new UserDocument();
    document.id = user.id();
    document.username = user.username();
    document.email = user.email();
    document.phoneNumber = user.phoneNumber();
    document.password = user.password();
    document.role = user.role();
    document.imageUrl = user.imageUrl();
    document.bio = user.bio();
    document.createdAt = user.createdAt();
    document.photoCount = user.photoCount();
    document.followerCount = user.followerCount();
    document.followingCount = user.followingCount();
    document.resetToken = user.resetToken();
    document.resetTokenExpiry = user.resetTokenExpiry();
    return document;
  }

  User toDomain() {
    return User.restore(
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
}
