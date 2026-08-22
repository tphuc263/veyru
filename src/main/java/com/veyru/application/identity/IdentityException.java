package com.veyru.application.identity;

public final class IdentityException extends RuntimeException {
  private final Reason reason;

  public IdentityException(Reason reason) {
    super(reason.name());
    this.reason = reason;
  }

  public Reason reason() {
    return reason;
  }

  public enum Reason {
    CONFLICT,
    VALIDATION_FAILED
  }
}
