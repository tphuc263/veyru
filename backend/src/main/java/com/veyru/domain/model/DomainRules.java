package com.veyru.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

final class DomainRules {
  private static final Pattern EMAIL =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

  private DomainRules() {}

  static String required(String value, String rule, String message) {
    if (value == null || value.isBlank()) throw new DomainValidationException(rule, message);
    return value.trim();
  }

  static String limited(String value, int max, String rule, String message) {
    if (value == null) return null;
    String normalized = value.trim();
    if (normalized.length() > max) throw new DomainValidationException(rule, message);
    return normalized;
  }

  static String email(String value) {
    String normalized =
        required(value, "user.email.required", "Email is required").toLowerCase(Locale.ROOT);
    if (!EMAIL.matcher(normalized).matches()) {
      throw new DomainValidationException("user.email.invalid", "Email format is invalid");
    }
    return normalized;
  }

  static long nonNegative(long value, String rule, String message) {
    if (value < 0) throw new DomainValidationException(rule, message);
    return value;
  }

  static void require(boolean condition, String rule, String message) {
    if (!condition) throw new DomainValidationException(rule, message);
  }
}
