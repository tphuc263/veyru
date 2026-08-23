package com.veyru.adapter.out.identity;

import com.veyru.application.port.out.MessagingUserLookup;
import com.veyru.application.port.out.UserStore;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RepositoryMessagingUserLookup implements MessagingUserLookup {
  private final UserStore userStore;

  public RepositoryMessagingUserLookup(UserStore userStore) {
    this.userStore = userStore;
  }

  @Override
  public Optional<UserSummary> findById(String userId) {
    return userStore
        .findById(userId)
        .map(user -> new UserSummary(user.getId(), user.getUsername(), user.getImageUrl()));
  }
}
