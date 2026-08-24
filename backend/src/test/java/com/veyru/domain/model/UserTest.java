package com.veyru.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {
  @Test
  void doesNotExposeSecretsInStringRepresentation() {
    User user = User.registered("alice", "alice@example.com", "password-hash", Instant.EPOCH);
    user.requestPasswordReset("reset-token", Instant.MAX);

    assertThat(user.toString()).doesNotContain("password-hash", "reset-token");
  }

  @Test
  void resetTokenLifecycleUsesProvidedTime() {
    Instant now = Instant.parse("2026-08-23T00:00:00Z");
    User user = User.registered("alice", "alice@example.com", "old-hash", now);
    user.requestPasswordReset("token", now.plusSeconds(60));

    assertThat(user.hasValidResetToken(now)).isTrue();
    assertThat(user.hasValidResetToken(now.plusSeconds(60))).isFalse();

    user.resetPassword("new-hash");
    assertThat(user.getPassword()).isEqualTo("new-hash");
    assertThat(user.hasValidResetToken(now)).isFalse();
  }
}
