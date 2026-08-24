package com.veyru.application.port.out;

import com.veyru.application.identity.AuthenticatedUser;
import java.time.Duration;
import java.util.Optional;

public interface RefreshSessionStore {
  record RefreshSession(AuthenticatedUser user, String token) {}

  RefreshSession create(AuthenticatedUser user, Duration ttl);

  Optional<RefreshSession> rotate(String token, Duration ttl);

  void revoke(String token);
}
