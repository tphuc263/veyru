package com.veyru.adapter.in.dto.response;

import com.veyru.application.common.PageResult;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
    List<T> items, int page, int size, long totalElements, int totalPages) {
  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  public static <T> PageResponse<T> from(PageResult<T> page) {
    return new PageResponse<>(
        page.items(), page.page(), page.size(), page.totalElements(), page.totalPages());
  }

  public static <T, R> PageResponse<R> from(PageResult<T> page, Function<T, R> mapper) {
    return new PageResponse<>(
        page.items().stream().map(mapper).toList(),
        page.page(), page.size(), page.totalElements(), page.totalPages());
  }
}
