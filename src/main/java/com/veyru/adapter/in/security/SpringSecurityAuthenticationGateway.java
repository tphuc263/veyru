package com.veyru.adapter.in.security;

import com.veyru.adapter.in.security.jwt.JwtUtils;
import com.veyru.adapter.in.security.userdetails.AppUserDetails;
import com.veyru.application.identity.LoginResult;
import com.veyru.application.port.out.AuthenticationGateway;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityAuthenticationGateway implements AuthenticationGateway {
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwt;

  public SpringSecurityAuthenticationGateway(
      AuthenticationManager authenticationManager, JwtUtils jwt) {
    this.authenticationManager = authenticationManager;
    this.jwt = jwt;
  }

  @Override
  public LoginResult authenticate(String identifier, String password) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(identifier, password));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
    return new LoginResult(
        jwt.generateAccessToken(authentication),
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst().orElse(""));
  }
}
