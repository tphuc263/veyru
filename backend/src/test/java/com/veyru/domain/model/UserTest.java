package com.veyru.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.veyru.domain.enums.UserRole;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {
  @Test
  void doesNotExposeSecretsInStringRepresentation() {
    User user = User.registered("alice", "alice@example.com", "password-hash", Instant.EPOCH);
    user = user.withPasswordResetRequested("reset-token", Instant.MAX);

    assertThat(user.toString()).doesNotContain("password-hash", "reset-token");
  }

  @Test
  void resetTokenLifecycleUsesProvidedTime() {
    Instant now = Instant.parse("2026-08-23T00:00:00Z");
    User user = User.registered("alice", "alice@example.com", "old-hash", now);
    user = user.withPasswordResetRequested("token", now.plusSeconds(60));

    assertThat(user.hasValidResetToken(now)).isTrue();
    assertThat(user.hasValidResetToken(now.plusSeconds(60))).isFalse();

    User reset = user.withResetPassword("new-hash");

    assertThat(user.password()).isEqualTo("old-hash");
    assertThat(reset.password()).isEqualTo("new-hash");
    assertThat(reset.hasValidResetToken(now)).isFalse();
  }

  @Test
  void profileUpdateReturnsANewAggregate() {
    User original = User.registered("alice", "ALICE@example.com", "hash", Instant.EPOCH);

    User updated = original.withUpdatedProfile("alice-new", "bio", null);

    assertThat(original.username()).isEqualTo("alice");
    assertThat(updated.username()).isEqualTo("alice-new");
    assertThat(updated.email()).isEqualTo("alice@example.com");
  }

  @Test
  void validatesAndNormalizesRegistrationFieldsWithStableRules() {
    User user = User.registered(" alice ", "ALICE@EXAMPLE.COM", "hash", Instant.EPOCH);

    assertThat(user.username()).isEqualTo("alice");
    assertThat(user.email()).isEqualTo("alice@example.com");
    assertThatThrownBy(() -> User.registered("a", "alice@example.com", "hash", Instant.EPOCH))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            exception -> assertThat(exception.rule()).isEqualTo("user.username.invalid-length"));
    assertThatThrownBy(() -> User.registered("alice", "invalid", "hash", Instant.EPOCH))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            exception -> assertThat(exception.rule()).isEqualTo("user.email.invalid"));
  }

  @Test
  void entityEqualityUsesOnlyPersistedIdentity() {
    User first = restoredUser("user-1", "alice");
    User sameIdentity = restoredUser("user-1", "different-name");
    User otherIdentity = restoredUser("user-2", "alice");
    User transientUser = User.registered("alice", "alice@example.com", "hash", Instant.EPOCH);

    assertThat(first).isEqualTo(sameIdentity).isNotEqualTo(otherIdentity);
    assertThat(transientUser)
        .isNotEqualTo(User.registered("alice", "alice@example.com", "hash", Instant.EPOCH));
  }

  private User restoredUser(String id, String username) {
    return User.restore(
        id,
        username,
        username + "@example.com",
        null,
        "hash",
        UserRole.ROLE_USER,
        null,
        null,
        Instant.EPOCH,
        0,
        0,
        0,
        null,
        null);
  }
}
