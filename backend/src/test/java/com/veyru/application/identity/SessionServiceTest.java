package com.veyru.application.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.port.out.AccessTokenIssuer;
import com.veyru.application.port.out.AuthenticationGateway;
import com.veyru.application.port.out.AuthorizationCodeStore;
import com.veyru.application.port.out.RefreshSessionStore;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionServiceTest {
  private final AuthenticationGateway authentication = mock(AuthenticationGateway.class);
  private final AccessTokenIssuer accessTokens = mock(AccessTokenIssuer.class);
  private final RefreshSessionStore refreshSessions = mock(RefreshSessionStore.class);
  private final AuthorizationCodeStore authorizationCodes = mock(AuthorizationCodeStore.class);
  private final AuthenticatedUser user =
      new AuthenticatedUser("user-1", "alice", "alice@example.com", "ROLE_USER");
  private final SessionService sessions =
      new SessionService(authentication, accessTokens, refreshSessions, authorizationCodes);

  @BeforeEach
  void configureTokens() {
    when(accessTokens.issue(user)).thenReturn("access-token");
    when(refreshSessions.create(any(), any()))
        .thenReturn(new RefreshSessionStore.RefreshSession(user, "refresh-token"));
  }

  @Test
  void loginCreatesAccessAndRefreshSession() {
    when(authentication.authenticate("alice", "password")).thenReturn(user);

    SessionTokens result = sessions.login(new LoginCommand("alice", "password"));

    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");
    assertThat(result.accessMaxAgeSeconds()).isEqualTo(900);
  }

  @Test
  void refreshRotatesInsteadOfCreatingAnotherSession() {
    when(refreshSessions.rotate(any(), any()))
        .thenReturn(Optional.of(new RefreshSessionStore.RefreshSession(user, "rotated-token")));

    assertThat(sessions.refresh("refresh-token").refreshToken()).isEqualTo("rotated-token");
  }

  @Test
  void oauthCodeCanOnlyBeExchangedWhenStoreConsumesIt() {
    when(authorizationCodes.consume("one-time-code"))
        .thenReturn(Optional.of(user), Optional.empty());

    assertThat(sessions.exchangeOAuthCode("one-time-code").user()).isEqualTo(user);
    assertThatThrownBy(() -> sessions.exchangeOAuthCode("one-time-code"))
        .isInstanceOf(UseCaseException.class);
  }

  @Test
  void logoutRevokesOnlyPresentedRefreshSession() {
    sessions.logout("refresh-token");

    verify(refreshSessions).revoke("refresh-token");
  }
}
