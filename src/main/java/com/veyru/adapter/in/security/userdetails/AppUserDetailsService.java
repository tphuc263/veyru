package com.veyru.adapter.in.security.userdetails;

import com.veyru.domain.model.User;
import com.veyru.application.port.out.UserRepository;
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
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String identifier)
      throws UsernameNotFoundException, DisabledException {
    log.debug("Loading user by identifier: {}", identifier);
    User user =
        userRepository
            .findByEmail(identifier)
            .or(() -> userRepository.findByUsername(identifier))
            .or(() -> userRepository.findByPhoneNumber(identifier))
            .orElseThrow(
                () -> {
                  log.warn("User not found with identifier: {}", identifier);
                  return new UsernameNotFoundException(
                      "User not found with identifier: " + identifier);
                });
    log.debug("User loaded successfully: {}", user.getUsername());
    return AppUserDetails.buildUserDetails(user);
  }

  public AppUserDetailsService(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }
}
