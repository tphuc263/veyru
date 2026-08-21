package com.veyru.domain.service.auth;

import com.veyru.adapter.in.dto.request.auth.ForgotPasswordRequest;
import com.veyru.adapter.in.dto.request.auth.LoginRequest;
import com.veyru.adapter.in.dto.request.auth.RegisterRequest;
import com.veyru.adapter.in.dto.request.auth.ResetPasswordRequest;
import com.veyru.adapter.in.dto.response.auth.LoginResponse;
import com.veyru.domain.enums.UserRole;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.User;
import com.veyru.application.port.out.UserRepository;
import com.veyru.adapter.in.security.jwt.JwtUtils;
import com.veyru.adapter.in.security.userdetails.AppUserDetails;
import com.veyru.domain.service.email.EmailService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtUtils;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

  public LoginResponse login(LoginRequest request) {
    log.info("Login attempt for identifier: {}", request.getIdentifier());
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getIdentifier(), request.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateAccessToken(authentication);
    AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
    log.info("Login successful for user: {}", userDetails.getUsername());
    return new LoginResponse(
        jwt,
        userDetails.getId(),
        userDetails.getUsername(),
        userDetails.getEmail(),
        userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse(""));
  }

  public void register(RegisterRequest request) {
    log.info("Registration attempt for email: {}", request.getEmail());
    if (userRepository.existsByEmail(request.getEmail())) {
      log.warn("Registration failed: Email already exists - {}", request.getEmail());
      throw new ApiException(ErrorCode.RESOURCE_CONFLICT);
    }
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(UserRole.ROLE_USER);
    user.setCreatedAt(Instant.now());
    userRepository.save(user);
    log.info("User registered successfully: {}", request.getEmail());
  }

  public void forgotPassword(ForgotPasswordRequest request) {
    log.info("Password reset requested for email: {}", request.getEmail());
    User user = userRepository.findByEmail(request.getEmail()).orElse(null);
    if (user == null) {
      log.info("Password reset requested for an unknown email");
      return;
    }
    // Generate new token and store on user
    String token = UUID.randomUUID().toString();
    user.setResetToken(token);
    user.setResetTokenExpiry(Instant.now().plus(RESET_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
    userRepository.save(user);
    // Send email asynchronously
    try {
      emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());
    } catch (RuntimeException ex) {
      log.error("Unable to send password reset email", ex);
    }
    log.info("Password reset token generated for user: {}", user.getEmail());
  }

  public void resetPassword(ResetPasswordRequest request) {
    log.info("Password reset attempt with token");
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new ApiException(ErrorCode.VALIDATION_FAILED);
    }
    User user =
        userRepository
            .findByResetToken(request.getToken())
            .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_FAILED));
    if (!user.isResetTokenValid()) {
      log.warn("Password reset failed: Token expired");
      throw new ApiException(ErrorCode.VALIDATION_FAILED);
    }
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    // Clear the reset token
    user.setResetToken(null);
    user.setResetTokenExpiry(null);
    userRepository.save(user);
    log.info("Password reset successful for user: {}", user.getEmail());
  }

  public boolean validateResetToken(String token) {
    return userRepository.findByResetToken(token).map(User::isResetTokenValid).orElse(false);
  }

  public AuthService(
      final AuthenticationManager authenticationManager,
      final JwtUtils jwtUtils,
      final UserRepository userRepository,
      final PasswordEncoder passwordEncoder,
      final EmailService emailService) {
    this.authenticationManager = authenticationManager;
    this.jwtUtils = jwtUtils;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
  }
}
