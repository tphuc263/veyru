package com.veyru.application.error;

public enum ErrorCode {
  AUTHENTICATION_REQUIRED("Authentication is required."), ACCESS_DENIED("You do not have permission to perform this action."),
  RESOURCE_NOT_FOUND("The requested resource was not found."), RESOURCE_CONFLICT("The request conflicts with the current resource state."),
  VALIDATION_FAILED("One or more fields are invalid."), EXTERNAL_SERVICE_FAILURE("A required external service failed.");
  private final String detail;
  ErrorCode(String detail) { this.detail = detail; }
  public String detail() { return detail; }
}
