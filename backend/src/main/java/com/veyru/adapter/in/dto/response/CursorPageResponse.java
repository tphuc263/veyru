package com.veyru.adapter.in.dto.response;

import com.veyru.application.common.CursorPageResult;
import java.util.List;
import java.util.function.Function;

public record CursorPageResponse<T>(List<T> items, String nextCursor, boolean hasMore) {
  public static <T, R> CursorPageResponse<R> from(CursorPageResult<T> page, Function<T, R> mapper) {
    return new CursorPageResponse<>(
        page.items().stream().map(mapper).toList(), page.nextCursor(), page.hasMore());
  }
}
