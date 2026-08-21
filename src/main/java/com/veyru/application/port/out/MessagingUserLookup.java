package com.veyru.application.port.out;

import java.util.Optional;

public interface MessagingUserLookup {
  Optional<UserSummary> findById(String userId);

  record UserSummary(String id, String username, String imageUrl) {}
}
