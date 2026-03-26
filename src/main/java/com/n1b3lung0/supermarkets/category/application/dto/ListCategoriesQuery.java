package com.n1b3lung0.supermarkets.category.application.dto;

import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** Query to list categories, optionally filtered by supermarket and/or level. */
public record ListCategoriesQuery(UUID supermarketId, String levelType, Pageable pageable) {

  public ListCategoriesQuery(Pageable pageable) {
    this(null, null, pageable);
  }
}
