package com.n1b3lung0.supermarkets.shared.application.mapper;

import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
import org.springframework.data.domain.Page;

/** Utility that converts Spring's {@link Page} into the framework-agnostic {@link PageResponse}. */
public final class PageResponseMapper {

  private PageResponseMapper() {}

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
