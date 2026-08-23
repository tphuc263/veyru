package com.veyru.config;

import com.veyru.application.identity.AuthenticationService;
import com.veyru.application.port.out.AuthenticationGateway;
import com.veyru.application.port.out.IdentityUserStore;
import com.veyru.application.port.out.MailSender;
import java.time.Clock;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class IdentityConfig {
  @Bean
  public AuthenticationService authenticationService(
      AuthenticationGateway authentication,
      IdentityUserStore users,
      PasswordEncoder passwords,
      MailSender mail,
      Clock clock) {
    return new AuthenticationService(
        authentication,
        users,
        passwords::encode,
        mail,
        () -> UUID.randomUUID().toString(),
        clock);
  }
}
