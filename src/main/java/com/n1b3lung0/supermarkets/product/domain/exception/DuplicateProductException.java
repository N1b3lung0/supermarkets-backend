package com.n1b3lung0.supermarkets.product.domain.exception;

import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.shared.domain.exception.ConflictException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;

/** Thrown when a product with the same externalId already exists for the given supermarket. */
public class DuplicateProductException extends ConflictException {

  public DuplicateProductException(ExternalProductId externalId, SupermarketId supermarketId) {
    super(
        "Product with externalId '"
            + externalId.value()
            + "' already exists for supermarket "
            + supermarketId.value());
  }
}
