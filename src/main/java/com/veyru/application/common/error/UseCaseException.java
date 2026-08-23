package com.veyru.application.common.error;

public class UseCaseException extends RuntimeException {
  private final UseCaseError code;

  public UseCaseException(UseCaseError code) {
    super(code.detail());
    this.code = code;
  }

  public UseCaseException(UseCaseError code, Throwable cause) {
    super(code.detail(), cause);
    this.code = code;
  }

  public UseCaseError code() {
    return code;
  }
}
