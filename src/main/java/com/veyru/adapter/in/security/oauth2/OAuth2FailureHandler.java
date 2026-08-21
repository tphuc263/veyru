package com.veyru.adapter.in.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {
  private static final Logger log = LoggerFactory.getLogger(OAuth2FailureHandler.class);

  @Value("${app.oauth2.failureRedirectUri}")
  private String defaultFailureRedirectUri;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    log.error("OAuth2 authentication failed: {}", exception.getMessage());
    String errorMessage = exception.getMessage();
    if (errorMessage == null || errorMessage.isEmpty()) {
      errorMessage = "Login failed, try again";
    }
    String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
    String redirectUrl =
        UriComponentsBuilder.fromUriString(defaultFailureRedirectUri)
            .queryParam("error", encodedErrorMessage)
            .build()
            .toUriString();
    log.info("Redirecting to: {}", redirectUrl);
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }

  public OAuth2FailureHandler() {}
}
