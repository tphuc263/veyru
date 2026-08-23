package com.veyru.application.identity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.veyru.application.port.out.AuthenticationGateway;
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
  void storesAndSendsPasswordResetUsingInjectedTimeAndToken() {
    Instant now = Instant.parse("2026-08-23T00:00:00Z");
    IdentityUserStore users = mock(IdentityUserStore.class);
    MailSender mail = mock(MailSender.class);
    User user = User.registered("alice", "alice@example.com", "hash", now);
    when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    AuthenticationService service =
        new AuthenticationService(
            mock(AuthenticationGateway.class),
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
