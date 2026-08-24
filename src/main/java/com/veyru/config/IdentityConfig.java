package com.veyru.config;

import com.veyru.application.identity.AuthenticationService;
import com.veyru.application.identity.SessionService;
import com.veyru.application.port.out.AccessTokenIssuer;
import com.veyru.application.port.out.AuthenticationGateway;
import com.veyru.application.port.out.AuthorizationCodeStore;
import com.veyru.application.port.out.IdentityUserStore;
import com.veyru.application.port.out.MailSender;
import com.veyru.application.port.out.RefreshSessionStore;
import java.time.Clock;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class IdentityConfig {
  @Bean
  public AuthenticationService authenticationService(
      IdentityUserStore users, PasswordEncoder passwords, MailSender mail, Clock clock) {
    return new AuthenticationService(
        users, passwords::encode, mail, () -> UUID.randomUUID().toString(), clock);
  }

  @Bean
  public SessionService sessionService(
      AuthenticationGateway authentication,
      AccessTokenIssuer accessTokens,
      RefreshSessionStore refreshSessions,
      AuthorizationCodeStore authorizationCodes) {
    return new SessionService(authentication, accessTokens, refreshSessions, authorizationCodes);
  }
}
