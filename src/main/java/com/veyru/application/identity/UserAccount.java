package com.veyru.application.identity;

import com.veyru.domain.enums.UserRole;
import java.time.Instant;

public record UserAccount(
    String id,
    String username,
    String email,
    String password,
    UserRole role,
    Instant createdAt,
    String resetToken,
    Instant resetTokenExpiry) {

  public static UserAccount registered(
      String username, String email, String password, Instant createdAt) {
    return new UserAccount(
        null, username, email, password, UserRole.ROLE_USER, createdAt, null, null);
  }

  public UserAccount requestPasswordReset(String token, Instant expiry) {
    return new UserAccount(id, username, email, password, role, createdAt, token, expiry);
  }

  public UserAccount resetPassword(String encodedPassword) {
    return new UserAccount(id, username, email, encodedPassword, role, createdAt, null, null);
  }

  public boolean hasValidResetToken(Instant now) {
    return resetToken != null && resetTokenExpiry != null && now.isBefore(resetTokenExpiry);
  }
}
