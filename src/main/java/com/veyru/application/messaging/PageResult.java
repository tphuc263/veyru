package com.veyru.application.messaging;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalElements, int totalPages) {

  public PageResult {
    items = List.copyOf(items);
  }
}
