package com.veyru.adapter.in.dto.response.auth;

import com.veyru.application.identity.LoginResult;

public record LoginResponse(String jwt, String id, String username, String email, String role) {
  public static LoginResponse from(LoginResult value) {
    return new LoginResponse(
        value.token(), value.id(), value.username(), value.email(), value.role());
  }
}
