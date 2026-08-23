package com.veyru.application.common;

public record PageQuery(int page, int size) {
  public PageQuery {
    if (page < 0 || size < 1) {
      throw new IllegalArgumentException("Invalid pagination");
    }
  }
}
