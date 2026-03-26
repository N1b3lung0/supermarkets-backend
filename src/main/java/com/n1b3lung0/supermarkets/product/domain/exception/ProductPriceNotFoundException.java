package com.n1b3lung0.supermarkets.product.domain.exception;

import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;

/** Thrown when a ProductPrice record is not found. */
public class ProductPriceNotFoundException extends NotFoundException {

  public ProductPriceNotFoundException(ProductId productId) {
    super("No price found for product id: " + productId.value());
  }
}
