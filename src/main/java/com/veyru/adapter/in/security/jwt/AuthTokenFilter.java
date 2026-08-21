package com.veyru.adapter.in.security.jwt;

import com.veyru.adapter.in.security.userdetails.AppUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);
  private final JwtUtils jwtUtils;
  private final AppUserDetailsService userDetailsService;
  private final JwtEntryPoint authEntryPoint;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String jwt = parseJwt(request);
      if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
        authenticateUser(jwt);
      }
    } catch (RuntimeException e) {
      log.debug("Invalid token for {} {}", request.getMethod(), request.getRequestURI());
      authEntryPoint.commence(request, response, new BadCredentialsException("Invalid token", e));
      return;
    }
    filterChain.doFilter(request, response);
  }

  private void authenticateUser(String jwt) {
    String username = jwtUtils.getEmailFromToken(jwt);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    var authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  public String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }
    return null;
  }

  public AuthTokenFilter(
      final JwtUtils jwtUtils,
      final AppUserDetailsService userDetailsService,
      final JwtEntryPoint authEntryPoint) {
    this.jwtUtils = jwtUtils;
    this.userDetailsService = userDetailsService;
    this.authEntryPoint = authEntryPoint;
  }
}
