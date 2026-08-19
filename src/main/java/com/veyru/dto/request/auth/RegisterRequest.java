package com.veyru.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
  @NotBlank(message = "Username must not be blank")
  @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
  private String username;

  @NotBlank(message = "Email must not be blank")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password must not be blank")
  @Size(min = 6, message = "Password must be at least 6 characters long")
  private String password;
}
