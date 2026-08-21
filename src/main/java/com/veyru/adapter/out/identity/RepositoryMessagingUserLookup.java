package com.veyru.adapter.out.identity;

import com.veyru.application.port.out.MessagingUserLookup;
import com.veyru.application.port.out.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RepositoryMessagingUserLookup implements MessagingUserLookup {
  private final UserRepository userRepository;

  public RepositoryMessagingUserLookup(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<UserSummary> findById(String userId) {
    return userRepository
        .findById(userId)
        .map(user -> new UserSummary(user.getId(), user.getUsername(), user.getImageUrl()));
  }
}
