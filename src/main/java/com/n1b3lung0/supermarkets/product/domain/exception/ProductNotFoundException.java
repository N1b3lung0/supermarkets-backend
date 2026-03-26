package com.n1b3lung0.supermarkets.product.domain.exception;

import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;

/** Thrown when a Product with the requested id does not exist (or has been deleted). */
public class ProductNotFoundException extends NotFoundException {

  public ProductNotFoundException(ProductId productId) {
    super("Product not found with id: " + productId.value());
  }
}
