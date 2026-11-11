package com.veyru.adapter.in.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.veyru.config.websocket.WebSocketSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.messaging.web.csrf.CsrfChannelInterceptor;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import tools.jackson.databind.ObjectMapper;

class JwtErrorHandlerTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void writesInvalidTokenProblem() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new JwtEntryPoint(objectMapper)
        .commence(
            new MockHttpServletRequest("GET", "/api/v1/me"),
            response,
            new BadCredentialsException("invalid"));

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
    assertThat(response.getContentType()).startsWith("application/problem+json");
    assertThat(response.getContentAsString()).contains("INVALID_TOKEN");
  }

  @Test
  void writesAccessDeniedProblem() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new JwtAccessDeniedHandler(objectMapper)
        .handle(
            new MockHttpServletRequest("GET", "/api/v1/me"),
            response,
            new AccessDeniedException("denied"));

    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentType()).startsWith("application/problem+json");
    assertThat(response.getContentAsString()).contains("ACCESS_DENIED");
  }

  @Test
  void writesInvalidCsrfTokenProblemForCsrfExceptions() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new JwtAccessDeniedHandler(objectMapper)
        .handle(
            new MockHttpServletRequest("PUT", "/api/v1/users/me/following/target"),
            response,
            new InvalidCsrfTokenException(
                new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "expected"), "stale"));

    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentType()).startsWith("application/problem+json");
    assertThat(response.getContentAsString()).contains("INVALID_CSRF_TOKEN");
  }

  @Test
  void usesPlainCsrfChannelInterceptorForWebSocketConnect() {
    assertThat(new WebSocketSecurityConfig().csrfChannelInterceptor())
        .isInstanceOf(CsrfChannelInterceptor.class);
  }
}
