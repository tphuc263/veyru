package com.veyru.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Identifier must not be blank") String identifier,
    @NotBlank(message = "Password must not be blank") String password) {
  public String getIdentifier() {
    return identifier;
  }

  public String getPassword() {
    return password;
  }
}
