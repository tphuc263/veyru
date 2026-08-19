package com.veyru.security.jwt;

import com.veyru.security.userdetails.AppUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

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
    } catch (Exception e) {
      log.error(
          "Cannot set user authentication for request: {} {}",
          request.getMethod(),
          request.getRequestURI(),
          e);
      authEntryPoint.commence(request, response, new BadCredentialsException("Invalid token", e));
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void authenticateUser(String jwt) {
    try {
      String username = jwtUtils.getEmailFromToken(jwt);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      var authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception e) {
      log.error("Failed to authenticate user from JWT token", e);
      throw e;
    }
  }

  public String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }
    return null;
  }
}
