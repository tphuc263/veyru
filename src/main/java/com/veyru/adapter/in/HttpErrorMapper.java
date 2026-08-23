package com.veyru.adapter.in;

import com.veyru.adapter.in.error.ErrorCode;
import org.springframework.http.HttpStatus;

public final class HttpErrorMapper {
  private HttpErrorMapper() {}

  public static HttpStatus status(ErrorCode code) {
    return switch (code) {
      case MALFORMED_REQUEST, MISSING_REQUEST_VALUE, INVALID_REQUEST_VALUE ->
          HttpStatus.BAD_REQUEST;
      case AUTHENTICATION_REQUIRED, INVALID_CREDENTIALS, INVALID_TOKEN -> HttpStatus.UNAUTHORIZED;
      case ACCESS_DENIED -> HttpStatus.FORBIDDEN;
      case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
      case NOT_ACCEPTABLE -> HttpStatus.NOT_ACCEPTABLE;
      case RESOURCE_CONFLICT -> HttpStatus.CONFLICT;
      case PAYLOAD_TOO_LARGE -> HttpStatus.CONTENT_TOO_LARGE;
      case UNSUPPORTED_MEDIA_TYPE -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
      case VALIDATION_FAILED -> HttpStatus.UNPROCESSABLE_CONTENT;
      case EXTERNAL_SERVICE_FAILURE -> HttpStatus.BAD_GATEWAY;
      case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
      case GATEWAY_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
      case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
