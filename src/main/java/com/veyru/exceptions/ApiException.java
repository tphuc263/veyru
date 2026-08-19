package com.veyru.exceptions;

public class ApiException extends RuntimeException {
  private final ErrorCode code;

  public ApiException(ErrorCode code) {
    super(code.detail());
    this.code = code;
  }

  public ApiException(ErrorCode code, Throwable cause) {
    super(code.detail(), cause);
    this.code = code;
  }

  public ErrorCode code() {
    return code;
  }
}
