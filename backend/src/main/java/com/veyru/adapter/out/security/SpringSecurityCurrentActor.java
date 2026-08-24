package com.veyru.adapter.out.security;

import com.veyru.adapter.security.AppUserDetails;
import com.veyru.application.port.out.CurrentActor;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentActor implements CurrentActor {
  @Override
  public Optional<String> id() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof AppUserDetails user)) {
      return Optional.empty();
    }
    return Optional.of(user.getId());
  }
}
