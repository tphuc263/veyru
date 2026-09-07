package com.veyru.domain.model;

/** Indicates that a value or state transition violates a domain invariant. */
public final class DomainValidationException extends IllegalArgumentException {
  private final String rule;

  public DomainValidationException(String rule, String message) {
    super(message);
    this.rule = rule;
  }

  public String rule() {
    return rule;
  }
}
