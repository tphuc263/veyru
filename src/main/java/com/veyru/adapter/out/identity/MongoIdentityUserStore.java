package com.veyru.adapter.out.identity;

import com.veyru.application.identity.UserAccount;
import com.veyru.application.port.out.IdentityUserStore;
import com.veyru.application.port.out.UserRepository;
import com.veyru.domain.model.User;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MongoIdentityUserStore implements IdentityUserStore {
  private final UserRepository repository;

  public MongoIdentityUserStore(UserRepository repository) {
    this.repository = repository;
  }

  @Override
  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email);
  }

  @Override
  public Optional<UserAccount> findByEmail(String email) {
    return repository.findByEmail(email).map(MongoIdentityUserStore::toAccount);
  }

  @Override
  public Optional<UserAccount> findByResetToken(String token) {
    return repository.findByResetToken(token).map(MongoIdentityUserStore::toAccount);
  }

  @Override
  public UserAccount save(UserAccount account) {
    User user =
        account.id() == null ? new User() : repository.findById(account.id()).orElseGet(User::new);
    user.setId(account.id());
    user.setUsername(account.username());
    user.setEmail(account.email());
    user.setPassword(account.password());
    user.setRole(account.role());
    user.setCreatedAt(account.createdAt());
    user.setResetToken(account.resetToken());
    user.setResetTokenExpiry(account.resetTokenExpiry());
    return toAccount(repository.save(user));
  }

  private static UserAccount toAccount(User user) {
    return new UserAccount(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        user.getRole(),
        user.getCreatedAt(),
        user.getResetToken(),
        user.getResetTokenExpiry());
  }
}
