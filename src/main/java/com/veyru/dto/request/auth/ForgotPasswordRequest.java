package com.veyru.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "Email must not be blank") @Email(message = "Invalid email format")
        String email) {
  public String getEmail() {
    return email;
  }
}
