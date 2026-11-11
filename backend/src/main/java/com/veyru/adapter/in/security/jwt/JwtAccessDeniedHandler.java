package com.veyru.adapter.in.security.jwt;

import com.veyru.adapter.in.HttpErrorMapper;
import com.veyru.adapter.in.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
      throws IOException {
    ErrorCode code =
        exception instanceof CsrfException ? ErrorCode.INVALID_CSRF_TOKEN : ErrorCode.ACCESS_DENIED;
    var status = HttpErrorMapper.status(code);
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, code.detail());
    problem.setTitle(status.getReasonPhrase());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", code.name());
    response.setStatus(problem.getStatus());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), problem);
  }

  public JwtAccessDeniedHandler(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
