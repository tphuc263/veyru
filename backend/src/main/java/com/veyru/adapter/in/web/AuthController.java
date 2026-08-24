package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.request.auth.ForgotPasswordRequest;
import com.veyru.adapter.in.dto.request.auth.RegisterRequest;
import com.veyru.adapter.in.dto.request.auth.ResetPasswordRequest;
import com.veyru.application.identity.AuthenticationService;
import com.veyru.application.identity.RegisterUserCommand;
import com.veyru.application.identity.ResetPasswordCommand;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}")
@SecurityRequirements
public class AuthController {
  private final AuthenticationService authentication;

  @PostMapping("/users")
  @ApiResponse(responseCode = "201", description = "User registered")
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
    authentication.register(
        new RegisterUserCommand(request.username(), request.email(), request.password()));
    return ResponseEntity.status(201).build();
  }

  @PostMapping("/password-reset-requests")
  @ApiResponse(responseCode = "202", description = "Reset request accepted")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authentication.forgotPassword(request.email());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/password-resets")
  @ApiResponse(responseCode = "204", description = "Password reset")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authentication.resetPassword(
        new ResetPasswordCommand(
            request.token(), request.newPassword(), request.confirmPassword()));
    return ResponseEntity.noContent().build();
  }

  public AuthController(final AuthenticationService authentication) {
    this.authentication = authentication;
  }
}
