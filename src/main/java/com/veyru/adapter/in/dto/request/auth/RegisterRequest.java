package com.veyru.adapter.in.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username must not be blank")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        String username,
    @NotBlank(message = "Email must not be blank") @Email(message = "Invalid email format")
        String email,
    @NotBlank(message = "Password must not be blank")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password) {
  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }
}
