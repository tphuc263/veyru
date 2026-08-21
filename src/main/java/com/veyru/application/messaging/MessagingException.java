package com.veyru.application.messaging;

public final class MessagingException extends RuntimeException {
  private final Reason reason;

  public MessagingException(Reason reason) {
    super(reason.name());
    this.reason = reason;
  }

  public Reason reason() {
    return reason;
  }

  public enum Reason {
    RESOURCE_NOT_FOUND,
    ACCESS_DENIED
  }
}
