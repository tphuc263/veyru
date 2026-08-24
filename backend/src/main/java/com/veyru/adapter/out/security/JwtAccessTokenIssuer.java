package com.veyru.adapter.out.security;

import com.veyru.adapter.security.JwtUtils;
import com.veyru.application.identity.AuthenticatedUser;
import com.veyru.application.port.out.AccessTokenIssuer;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {
  private final JwtUtils jwt;

  public JwtAccessTokenIssuer(JwtUtils jwt) {
    this.jwt = jwt;
  }

  @Override
  public String issue(AuthenticatedUser user) {
    return jwt.generateToken(user.email(), user.id(), user.role());
  }
}
