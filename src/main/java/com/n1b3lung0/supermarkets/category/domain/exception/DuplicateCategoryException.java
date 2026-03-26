package com.n1b3lung0.supermarkets.category.domain.exception;

import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.shared.domain.exception.ConflictException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;

/**
 * Thrown when a Category with the same externalId already exists for a supermarket. Maps to HTTP
 * 409.
 */
public class DuplicateCategoryException extends ConflictException {

  public DuplicateCategoryException(ExternalCategoryId externalId, SupermarketId supermarketId) {
    super(
        "Category with externalId '"
            + externalId
            + "' already exists for supermarket: "
            + supermarketId);
  }
}
