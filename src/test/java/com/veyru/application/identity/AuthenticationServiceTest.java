package com.veyru.application.identity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.veyru.application.port.out.AuthenticationGateway;
import com.veyru.application.port.out.IdentityUserStore;
import com.veyru.application.port.out.MailSender;
import com.veyru.domain.enums.UserRole;
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
    UserAccount user =
        new UserAccount(
            "user-id", "alice", "alice@example.com", "hash", UserRole.ROLE_USER, now, null, null);
    when(users.findByEmail(user.email())).thenReturn(Optional.of(user));
    AuthenticationService service =
        new AuthenticationService(
            mock(AuthenticationGateway.class),
            users,
            password -> "hashed-" + password,
            mail,
            () -> "reset-token",
            Clock.fixed(now, ZoneOffset.UTC));

    service.forgotPassword(user.email());

    verify(users)
        .save(user.requestPasswordReset("reset-token", Instant.parse("2026-08-23T00:30:00Z")));
    verify(mail).sendPasswordReset(user.email(), "reset-token", user.username());
  }
}
