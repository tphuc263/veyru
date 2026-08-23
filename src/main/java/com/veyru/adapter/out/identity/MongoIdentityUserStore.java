package com.veyru.adapter.out.identity;

import com.veyru.application.port.out.IdentityUserStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.User;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MongoIdentityUserStore implements IdentityUserStore {
  private final UserStore repository;

  public MongoIdentityUserStore(UserStore repository) {
    this.repository = repository;
  }

  @Override
  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return repository.findByEmail(email);
  }

  @Override
  public Optional<User> findByResetToken(String token) {
    return repository.findByResetToken(token);
  }

  @Override
  public User save(User user) {
    return repository.save(user);
  }
}
