package com.veyru.adapter.in.security.userdetails;

import com.veyru.adapter.security.AppUserDetails;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
  private static final Logger log = LoggerFactory.getLogger(AppUserDetailsService.class);
  private final UserStore userStore;

  @Override
  public UserDetails loadUserByUsername(String identifier)
      throws UsernameNotFoundException, DisabledException {
    log.debug("Loading user by identifier: {}", identifier);
    User user =
        userStore
            .findByEmail(identifier)
            .or(() -> userStore.findByUsername(identifier))
            .or(() -> userStore.findByPhoneNumber(identifier))
            .orElseThrow(
                () -> {
                  log.warn("User not found with identifier: {}", identifier);
                  return new UsernameNotFoundException(
                      "User not found with identifier: " + identifier);
                });
    log.debug("User loaded successfully: {}", user.getUsername());
    return AppUserDetails.buildUserDetails(user);
  }

  public AppUserDetailsService(final UserStore userStore) {
    this.userStore = userStore;
  }
}
