package com.veyru.adapter.in.security.userdetails;

import com.veyru.adapter.security.AppUserDetails;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.User;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
  private final UserStore userStore;

  @Override
  public UserDetails loadUserByUsername(String identifier)
      throws UsernameNotFoundException, DisabledException {
    User user =
        userStore
            .findByEmail(identifier)
            .or(() -> userStore.findByUsername(identifier))
            .or(() -> userStore.findByPhoneNumber(identifier))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return AppUserDetails.buildUserDetails(user);
  }

  public AppUserDetailsService(final UserStore userStore) {
    this.userStore = userStore;
  }
}
