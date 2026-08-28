package com.veyru.adapter.security;

import com.veyru.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
  private final SecretKey signingKey;
  private final long expireTime;
  private final Clock clock;

  private SecretKey getSigningKey() {
    return signingKey;
  }

  public String generateToken(String email, String userId, String role) {
    return Jwts.builder()
        .subject(email)
        .claim("id", userId)
        .claim("roles", java.util.List.of(role))
        .issuedAt(Date.from(clock.instant()))
        .expiration(Date.from(clock.instant().plusMillis(expireTime)))
        .id(UUID.randomUUID().toString())
        .signWith(getSigningKey())
        .compact();
  }

  public String getEmailFromToken(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
      return claims.getSubject();
    } catch (JwtException | IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid or expired JWT token", e);
    }
  }

  public String getUserIdFromToken(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
      return claims.get("id", String.class);
    } catch (JwtException | IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid or expired JWT token", e);
    }
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  public JwtUtils(AuthProperties properties, Clock clock) {
    this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.token().jwtSecret()));
    this.expireTime = properties.token().accessExpiration().toMillis();
    this.clock = clock;
  }
}
