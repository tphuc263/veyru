package com.veyru.domain.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "The request is malformed."),
  MISSING_REQUEST_VALUE(HttpStatus.BAD_REQUEST, "A required request value is missing."),
  INVALID_REQUEST_VALUE(HttpStatus.BAD_REQUEST, "A request value has an invalid type."),
  AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Authentication is required."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "The access token is invalid or expired."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found."),
  METHOD_NOT_ALLOWED(
      HttpStatus.METHOD_NOT_ALLOWED, "The HTTP method is not supported for this resource."),
  NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "The requested response format is not available."),
  RESOURCE_CONFLICT(HttpStatus.CONFLICT, "The request conflicts with the current resource state."),
  PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "The request payload is too large."),
  UNSUPPORTED_MEDIA_TYPE(
      HttpStatus.UNSUPPORTED_MEDIA_TYPE, "The request media type is not supported."),
  VALIDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "One or more fields are invalid."),
  EXTERNAL_SERVICE_FAILURE(HttpStatus.BAD_GATEWAY, "A required external service failed."),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "The service is temporarily unavailable."),
  GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "A required service timed out."),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");

  private final HttpStatus status;
  private final String detail;

  ErrorCode(HttpStatus status, String detail) {
    this.status = status;
    this.detail = detail;
  }

  public HttpStatus status() {
    return status;
  }

  public String detail() {
    return detail;
  }
}
