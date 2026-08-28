package com.veyru.adapter.in.web;

import com.veyru.application.identity.SessionTokens;
import com.veyru.config.AuthProperties;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SessionCookieManager {
  public static final String ACCESS_COOKIE = "veyru_access";
  public static final String REFRESH_COOKIE = "veyru_refresh";
  private final boolean secure;
  private final String refreshPath;

  public SessionCookieManager(AuthProperties properties, @Value("${api.prefix}") String apiPrefix) {
    this.secure = properties.cookie().secure();
    this.refreshPath = apiPrefix + "/sessions";
  }

  public void addSessionCookies(ResponseEntity.HeadersBuilder<?> response, SessionTokens tokens) {
    response.header(
        HttpHeaders.SET_COOKIE,
        cookie(ACCESS_COOKIE, tokens.accessToken(), "/", tokens.accessMaxAgeSeconds()).toString());
    response.header(
        HttpHeaders.SET_COOKIE,
        cookie(REFRESH_COOKIE, tokens.refreshToken(), refreshPath, Duration.ofDays(30).toSeconds())
            .toString());
  }

  public void clearSessionCookies(ResponseEntity.HeadersBuilder<?> response) {
    response.header(HttpHeaders.SET_COOKIE, cookie(ACCESS_COOKIE, "", "/", 0).toString());
    response.header(HttpHeaders.SET_COOKIE, cookie(REFRESH_COOKIE, "", refreshPath, 0).toString());
  }

  private ResponseCookie cookie(String name, String value, String path, long maxAgeSeconds) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite("Lax")
        .path(path)
        .maxAge(maxAgeSeconds)
        .build();
  }
}
