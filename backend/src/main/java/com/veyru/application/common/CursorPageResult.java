package com.veyru.application.common;

import java.util.List;

public record CursorPageResult<T>(List<T> items, String nextCursor, boolean hasMore) {
  public CursorPageResult {
    items = List.copyOf(items);
  }
}
