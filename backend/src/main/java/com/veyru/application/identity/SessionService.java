package com.veyru.application.identity;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.port.out.AccessTokenIssuer;
import com.veyru.application.port.out.AuthenticationGateway;
import com.veyru.application.port.out.AuthorizationCodeStore;
import com.veyru.application.port.out.RefreshSessionStore;
import java.time.Duration;

public final class SessionService {
  private static final Duration ACCESS_TTL = Duration.ofMinutes(15);
  private static final Duration REFRESH_TTL = Duration.ofDays(30);
  private static final Duration OAUTH_CODE_TTL = Duration.ofSeconds(60);

  private final AuthenticationGateway authentication;
  private final AccessTokenIssuer accessTokens;
  private final RefreshSessionStore refreshSessions;
  private final AuthorizationCodeStore authorizationCodes;

  public SessionService(
      AuthenticationGateway authentication,
      AccessTokenIssuer accessTokens,
      RefreshSessionStore refreshSessions,
      AuthorizationCodeStore authorizationCodes) {
    this.authentication = authentication;
    this.accessTokens = accessTokens;
    this.refreshSessions = refreshSessions;
    this.authorizationCodes = authorizationCodes;
  }

  public SessionTokens login(LoginCommand command) {
    return createSession(authentication.authenticate(command.identifier(), command.password()));
  }

  public SessionTokens exchangeOAuthCode(String code) {
    AuthenticatedUser user =
        authorizationCodes
            .consume(code)
            .orElseThrow(() -> new UseCaseException(UseCaseError.AUTHENTICATION_REQUIRED));
    return createSession(user);
  }

  public SessionTokens refresh(String refreshToken) {
    RefreshSessionStore.RefreshSession rotated =
        refreshSessions
            .rotate(refreshToken, REFRESH_TTL)
            .orElseThrow(() -> new UseCaseException(UseCaseError.AUTHENTICATION_REQUIRED));
    return tokens(rotated.user(), rotated.token());
  }

  public void logout(String refreshToken) {
    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshSessions.revoke(refreshToken);
    }
  }

  public String issueOAuthCode(AuthenticatedUser user) {
    return authorizationCodes.issue(user, OAUTH_CODE_TTL);
  }

  private SessionTokens createSession(AuthenticatedUser user) {
    var refreshSession = refreshSessions.create(user, REFRESH_TTL);
    return tokens(user, refreshSession.token());
  }

  private SessionTokens tokens(AuthenticatedUser user, String refreshToken) {
    return new SessionTokens(user, accessTokens.issue(user), refreshToken, ACCESS_TTL.toSeconds());
  }
}
