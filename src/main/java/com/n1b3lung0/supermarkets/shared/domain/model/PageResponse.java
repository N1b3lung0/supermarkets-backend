package com.n1b3lung0.supermarkets.shared.domain.model;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Generic paginated response record. Wraps Spring's {@link Page} into a framework-agnostic
 * structure that can be safely exposed via REST.
 */
public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {

  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast());
  }
}
