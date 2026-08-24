package com.veyru.adapter.in.dto.response.auth;

import com.veyru.adapter.security.AppUserDetails;
import com.veyru.application.identity.AuthenticatedUser;

public record AuthenticatedUserResponse(String id, String username, String email, String role) {
  public static AuthenticatedUserResponse from(AuthenticatedUser user) {
    return new AuthenticatedUserResponse(user.id(), user.username(), user.email(), user.role());
  }

  public static AuthenticatedUserResponse from(AppUserDetails user) {
    return new AuthenticatedUserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getAuthorities().iterator().next().getAuthority());
  }
}
