package com.veyru.application.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.argThat;
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
            .withPasswordResetRequested("reset-token", now.plusSeconds(60));
    when(users.findByResetToken("reset-token")).thenReturn(Optional.of(user));
    AuthenticationService service =
        new AuthenticationService(
            users,
            password -> "hashed-" + password,
            mock(MailSender.class),
            () -> "unused",
            Clock.fixed(now, ZoneOffset.UTC));

    service.resetPassword(new ResetPasswordCommand("reset-token", "new-password", "new-password"));

    assertThat(user.password()).isEqualTo("hash");
    assertThat(user.resetToken()).isEqualTo("reset-token");
    verify(users)
        .save(
            argThat(
                saved ->
                    saved.password().equals("hashed-new-password") && saved.resetToken() == null));
  }

  @Test
  void storesAndSendsPasswordResetUsingInjectedTimeAndToken() {
    Instant now = Instant.parse("2026-08-23T00:00:00Z");
    IdentityUserStore users = mock(IdentityUserStore.class);
    MailSender mail = mock(MailSender.class);
    User user = User.registered("alice", "alice@example.com", "hash", now);
    when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
    AuthenticationService service =
        new AuthenticationService(
            users,
            password -> "hashed-" + password,
            mail,
            () -> "reset-token",
            Clock.fixed(now, ZoneOffset.UTC));

    service.forgotPassword(user.email());

    verify(users)
        .save(
            argThat(
                saved ->
                    "reset-token".equals(saved.resetToken())
                        && Instant.parse("2026-08-23T00:30:00Z").equals(saved.resetTokenExpiry())));
    verify(mail).sendPasswordReset(user.email(), "reset-token", user.username());
  }
}
