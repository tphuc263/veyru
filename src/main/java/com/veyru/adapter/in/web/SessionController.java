package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.request.auth.LoginRequest;
import com.veyru.adapter.in.dto.request.auth.OAuthCodeExchangeRequest;
import com.veyru.adapter.in.dto.response.auth.AuthenticatedUserResponse;
import com.veyru.adapter.security.AppUserDetails;
import com.veyru.application.identity.LoginCommand;
import com.veyru.application.identity.SessionService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/sessions")
public class SessionController {
  private final SessionService sessions;
  private final SessionCookieManager cookies;

  public SessionController(SessionService sessions, SessionCookieManager cookies) {
    this.sessions = sessions;
    this.cookies = cookies;
  }

  @PostMapping
  @SecurityRequirements
  public ResponseEntity<AuthenticatedUserResponse> login(@Valid @RequestBody LoginRequest request) {
    var tokens = sessions.login(new LoginCommand(request.identifier(), request.password()));
    var response = ResponseEntity.ok();
    cookies.addSessionCookies(response, tokens);
    return response.body(AuthenticatedUserResponse.from(tokens.user()));
  }

  @GetMapping("/current")
  public ResponseEntity<AuthenticatedUserResponse> current(
      @AuthenticationPrincipal AppUserDetails user) {
    return ResponseEntity.ok(AuthenticatedUserResponse.from(user));
  }

  @PostMapping("/refresh")
  @SecurityRequirements
  @ApiResponse(responseCode = "204", description = "Session refreshed")
  public ResponseEntity<Void> refresh(
      @CookieValue(name = SessionCookieManager.REFRESH_COOKIE, required = false)
          String refreshToken) {
    var tokens = sessions.refresh(refreshToken);
    var response = ResponseEntity.noContent();
    cookies.addSessionCookies(response, tokens);
    return response.build();
  }

  @PostMapping("/oauth2")
  @SecurityRequirements
  public ResponseEntity<AuthenticatedUserResponse> exchangeOAuthCode(
      @Valid @RequestBody OAuthCodeExchangeRequest request) {
    var tokens = sessions.exchangeOAuthCode(request.code());
    var response = ResponseEntity.ok();
    cookies.addSessionCookies(response, tokens);
    return response.body(AuthenticatedUserResponse.from(tokens.user()));
  }

  @DeleteMapping("/current")
  @SecurityRequirements
  @ApiResponse(responseCode = "204", description = "Current session revoked")
  public ResponseEntity<Void> logout(
      @CookieValue(name = SessionCookieManager.REFRESH_COOKIE, required = false)
          String refreshToken) {
    sessions.logout(refreshToken);
    var response = ResponseEntity.noContent();
    cookies.clearSessionCookies(response);
    return response.build();
  }
}
