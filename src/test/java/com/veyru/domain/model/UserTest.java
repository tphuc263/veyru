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
}
