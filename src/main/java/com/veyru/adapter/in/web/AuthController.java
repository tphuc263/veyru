package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.request.auth.ForgotPasswordRequest;
import com.veyru.adapter.in.dto.request.auth.LoginRequest;
import com.veyru.adapter.in.dto.request.auth.RegisterRequest;
import com.veyru.adapter.in.dto.request.auth.ResetPasswordRequest;
import com.veyru.adapter.in.dto.response.auth.LoginResponse;
import com.veyru.application.identity.AuthenticationService;
import com.veyru.application.identity.LoginCommand;
import com.veyru.application.identity.LoginResult;
import com.veyru.application.identity.RegisterUserCommand;
import com.veyru.application.identity.ResetPasswordCommand;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}")
public class AuthController {
  private final AuthenticationService authentication;

  @PostMapping("/sessions")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResult result =
        authentication.login(new LoginCommand(request.identifier(), request.password()));
    return ResponseEntity.ok(LoginResponse.from(result));
  }

  @PostMapping("/users")
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
    authentication.register(
        new RegisterUserCommand(request.username(), request.email(), request.password()));
    return ResponseEntity.status(201).build();
  }

  @PostMapping("/password-reset-requests")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authentication.forgotPassword(request.email());
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/password-resets")
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
