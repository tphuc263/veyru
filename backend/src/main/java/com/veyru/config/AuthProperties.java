package com.veyru.config;

import io.jsonwebtoken.io.Decoders;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth")
public record AuthProperties(@NotNull @Valid Token token, @NotNull @Valid Cookie cookie) {
  public record Token(@NotBlank String jwtSecret, @NotNull Duration accessExpiration) {
    @AssertTrue(message = "JWT secret must be valid Base64 encoding at least 32 bytes")
    public boolean isJwtSecretStrong() {
      try {
        return jwtSecret != null && Decoders.BASE64.decode(jwtSecret).length >= 32;
      } catch (RuntimeException exception) {
        return false;
      }
    }

    @AssertTrue(message = "Access expiration must be positive")
    public boolean isAccessExpirationPositive() {
      return accessExpiration != null
          && !accessExpiration.isZero()
          && !accessExpiration.isNegative();
    }
  }

  public record Cookie(boolean secure) {}
}
