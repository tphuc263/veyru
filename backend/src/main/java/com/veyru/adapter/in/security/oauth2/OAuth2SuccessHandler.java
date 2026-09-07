package com.veyru.adapter.in.security.oauth2;

import com.veyru.application.identity.AuthenticatedUser;
import com.veyru.application.identity.SessionService;
import com.veyru.config.ApplicationProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  private final SessionService sessions;

  private final String defaultRedirectUri;
  private final String defaultFailureRedirectUri;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    if (!(authentication.getPrincipal() instanceof CustomOAuth2User customUser)) {
      sendErrorRedirect(response, "invalid_principal_type");
      return;
    }
    String email = customUser.getEmail();
    if (email == null || email.isEmpty()) {
      sendErrorRedirect(response, "email_not_found");
      return;
    }
    String code =
        sessions.issueOAuthCode(
            new AuthenticatedUser(
                customUser.getUser().id(),
                customUser.getUser().username(),
                email,
                customUser.getUser().role().name()));
    String redirectUrl =
        UriComponentsBuilder.fromUriString(defaultRedirectUri)
            .queryParam("code", code)
            .build()
            .toUriString();
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }

  private void sendErrorRedirect(HttpServletResponse response, String code) throws IOException {
    String errorMsg = URLEncoder.encode("OAuth2 error: " + code, StandardCharsets.UTF_8);
    String redirectUrl =
        UriComponentsBuilder.fromUriString(defaultFailureRedirectUri)
            .queryParam("error", errorMsg)
            .build()
            .toUriString();
    response.sendRedirect(redirectUrl);
  }

  public OAuth2SuccessHandler(SessionService sessions, ApplicationProperties properties) {
    this.sessions = sessions;
    this.defaultRedirectUri = properties.oauth2().redirectUri().toString();
    this.defaultFailureRedirectUri = properties.oauth2().failureRedirectUri().toString();
  }
}
