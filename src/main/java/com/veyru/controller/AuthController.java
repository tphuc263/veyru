package com.veyru.controller;

import com.veyru.dto.request.auth.ForgotPasswordRequest;
import com.veyru.dto.request.auth.LoginRequest;
import com.veyru.dto.request.auth.RegisterRequest;
import com.veyru.dto.request.auth.ResetPasswordRequest;
import com.veyru.dto.response.auth.LoginResponse;
import com.veyru.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse loginResponse = authService.login(request);
    return ResponseEntity.ok(loginResponse);
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);
    return ResponseEntity.status(201).build();
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request);
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/validate-reset-token")
  public ResponseEntity<Boolean> validateResetToken(@RequestParam String token) {
    boolean isValid = authService.validateResetToken(token);
    return ResponseEntity.ok(isValid);
  }
}
