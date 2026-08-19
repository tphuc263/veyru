package com.veyru.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank(message = "Identifier must not be blank")
  private String identifier; // Can be email, username, or phone number

  @NotBlank(message = "Password must not be blank")
  private String password;
}
