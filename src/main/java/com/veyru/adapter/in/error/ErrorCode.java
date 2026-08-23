package com.veyru.adapter.in.error;

public enum ErrorCode {
  MALFORMED_REQUEST("The request is malformed."),
  MISSING_REQUEST_VALUE("A required request value is missing."),
  INVALID_REQUEST_VALUE("A request value has an invalid type."),
  AUTHENTICATION_REQUIRED("Authentication is required."),
  INVALID_CREDENTIALS("Invalid credentials."),
  INVALID_TOKEN("The access token is invalid or expired."),
  ACCESS_DENIED("You do not have permission to perform this action."),
  RESOURCE_NOT_FOUND("The requested resource was not found."),
  METHOD_NOT_ALLOWED("The HTTP method is not supported for this resource."),
  NOT_ACCEPTABLE("The requested response format is not available."),
  RESOURCE_CONFLICT("The request conflicts with the current resource state."),
  PAYLOAD_TOO_LARGE("The request payload is too large."),
  UNSUPPORTED_MEDIA_TYPE("The request media type is not supported."),
  VALIDATION_FAILED("One or more fields are invalid."),
  EXTERNAL_SERVICE_FAILURE("A required external service failed."),
  SERVICE_UNAVAILABLE("The service is temporarily unavailable."),
  GATEWAY_TIMEOUT("A required service timed out."),
  INTERNAL_ERROR("An unexpected error occurred.");

  private final String detail;

  ErrorCode(String detail) {
    this.detail = detail;
  }

  public String detail() {
    return detail;
  }
}
