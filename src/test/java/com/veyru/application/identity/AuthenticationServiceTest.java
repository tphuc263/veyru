package com.veyru.application.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.veyru.application.port.out.IdentityUserStore;
import com.veyru.application.port.out.MailSender;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthenticationServiceTest {
  @Test
  void resetsPasswordOnlyWhenTokenIsValid() {
    Instant now = Instant.parse("2026-08-23T00:00:00Z");
    IdentityUserStore users = mock(IdentityUserStore.class);
    User user =
        User.registered("alice", "alice@example.com", "hash", now)
            .requestPasswordReset("reset-token", now.plusSeconds(60));
    when(users.findByResetToken("reset-token")).thenReturn(Optional.of(user));
    AuthenticationService service =
        new AuthenticationService(
            users,
            password -> "hashed-" + password,
            mock(MailSender.class),
            () -> "unused",
            Clock.fixed(now, ZoneOffset.UTC));

    service.resetPassword(new ResetPasswordCommand("reset-token", "new-password", "new-password"));

    assertThat(user.getPassword()).isEqualTo("hashed-new-password");
    assertThat(user.getResetToken()).isNull();
    verify(users).save(user);
  }

  @Test
  void storesAndSendsPasswordResetUsingInjectedTimeAndToken() {
    Instant now = Instant.parse("2026-08-23T00:00:00Z");
    IdentityUserStore users = mock(IdentityUserStore.class);
    MailSender mail = mock(MailSender.class);
    User user = User.registered("alice", "alice@example.com", "hash", now);
    when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    AuthenticationService service =
        new AuthenticationService(
            users,
            password -> "hashed-" + password,
            mail,
            () -> "reset-token",
            Clock.fixed(now, ZoneOffset.UTC));

    service.forgotPassword(user.getEmail());

    verify(users)
        .save(user.requestPasswordReset("reset-token", Instant.parse("2026-08-23T00:30:00Z")));
    verify(mail).sendPasswordReset(user.getEmail(), "reset-token", user.getUsername());
  }
}
