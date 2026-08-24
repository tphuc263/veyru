package com.veyru.application.port.out;

import com.veyru.application.identity.AuthenticatedUser;
import java.time.Duration;
import java.util.Optional;

public interface AuthorizationCodeStore {
  String issue(AuthenticatedUser user, Duration ttl);

  Optional<AuthenticatedUser> consume(String code);
}
