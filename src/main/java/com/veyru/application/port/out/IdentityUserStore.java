package com.veyru.application.port.out;

import com.veyru.domain.model.User;
import java.util.Optional;

public interface IdentityUserStore {
  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  Optional<User> findByResetToken(String token);

  User save(User user);
}
