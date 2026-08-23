package com.veyru.adapter.in.security.jwt;

import com.veyru.adapter.in.HttpErrorMapper;
import com.veyru.adapter.in.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    boolean invalidToken = exception instanceof BadCredentialsException;
    ErrorCode code = invalidToken ? ErrorCode.INVALID_TOKEN : ErrorCode.AUTHENTICATION_REQUIRED;
    var status = HttpErrorMapper.status(code);
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, code.detail());
    problem.setTitle(status.getReasonPhrase());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", code.name());
    response.setStatus(problem.getStatus());
    response.setHeader("WWW-Authenticate", "Bearer");
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), problem);
  }

  public JwtEntryPoint(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
