package com.veyru.adapter.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public final class UserIdAuthenticationToken extends UsernamePasswordAuthenticationToken {
  public UserIdAuthenticationToken(AppUserDetails principal) {
    super(principal, null, principal.getAuthorities());
  }

  @Override
  public String getName() {
    return ((AppUserDetails) getPrincipal()).getId();
  }
}
