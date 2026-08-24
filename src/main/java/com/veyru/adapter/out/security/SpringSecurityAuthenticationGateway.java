package com.veyru.adapter.out.security;

import com.veyru.adapter.security.AppUserDetails;
import com.veyru.application.identity.AuthenticatedUser;
import com.veyru.application.port.out.AuthenticationGateway;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuthenticationGateway implements AuthenticationGateway {
  private final DaoAuthenticationProvider authenticationProvider;

  public SpringSecurityAuthenticationGateway(DaoAuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
  }

  @Override
  public AuthenticatedUser authenticate(String identifier, String password) {
    Authentication authentication =
        authenticationProvider.authenticate(
            new UsernamePasswordAuthenticationToken(identifier, password));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
    return new AuthenticatedUser(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getAuthorities().iterator().next().getAuthority());
  }
}
