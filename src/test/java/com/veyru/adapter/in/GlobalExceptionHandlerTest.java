package com.veyru.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
  private final ServletWebRequest request =
      new ServletWebRequest(new MockHttpServletRequest("GET", "/api/v1/comments/1"));

  @Test
  void mapsSemanticErrorsToTheirHttpContract() {
    assertProblem(
        handler.handleApiException(new ApiException(ErrorCode.RESOURCE_NOT_FOUND), request),
        HttpStatus.NOT_FOUND,
        "RESOURCE_NOT_FOUND");
    assertProblem(
        handler.handleApiException(new ApiException(ErrorCode.ACCESS_DENIED), request),
        HttpStatus.FORBIDDEN,
        "ACCESS_DENIED");
    assertProblem(
        handler.handleApiException(new ApiException(ErrorCode.RESOURCE_CONFLICT), request),
        HttpStatus.CONFLICT,
        "RESOURCE_CONFLICT");
    assertProblem(
        handler.handleApiException(new ApiException(ErrorCode.EXTERNAL_SERVICE_FAILURE), request),
        HttpStatus.BAD_GATEWAY,
        "EXTERNAL_SERVICE_FAILURE");
  }

  @Test
  void mapsValidationAndUnexpectedErrorsWithoutLeakingDetails() {
    assertProblem(
        handler.handleInvalidValue(new IllegalArgumentException("secret"), request),
        HttpStatus.UNPROCESSABLE_ENTITY,
        "VALIDATION_FAILED");
    ProblemDetail problem = handler.handleUnexpected(new RuntimeException("secret"), request);
    assertProblem(problem, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    assertThat(problem.getDetail()).isEqualTo(ErrorCode.INTERNAL_ERROR.detail());
  }

  private void assertProblem(ProblemDetail problem, HttpStatus status, String code) {
    assertThat(problem.getStatus()).isEqualTo(status.value());
    assertThat(problem.getProperties()).containsEntry("code", code);
    assertThat(problem.getInstance().getPath()).isEqualTo("/api/v1/comments/1");
  }
}
