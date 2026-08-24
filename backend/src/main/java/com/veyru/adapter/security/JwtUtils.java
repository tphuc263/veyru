package com.veyru.adapter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
  @Value("${auth.token.jwtSecret}")
  private String jwtSecret;

  @Value("${auth.token.accessExpirationInMils:86400000}")
  private Long expireTime;

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(String email, String userId, String role) {
    return Jwts.builder()
        .subject(email)
        .claim("id", userId)
        .claim("roles", java.util.List.of(role))
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expireTime))
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
}
