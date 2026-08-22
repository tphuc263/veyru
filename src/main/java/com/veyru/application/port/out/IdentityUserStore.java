package com.veyru.application.port.out;

import com.veyru.application.identity.UserAccount;
import java.util.Optional;

public interface IdentityUserStore {
  boolean existsByEmail(String email);

  Optional<UserAccount> findByEmail(String email);

  Optional<UserAccount> findByResetToken(String token);

  UserAccount save(UserAccount user);
}
