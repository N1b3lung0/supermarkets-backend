package com.n1b3lung0.supermarkets.supermarket.domain.exception;

import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;

/** Thrown when a Supermarket cannot be found by the given identifier. */
public class SupermarketNotFoundException extends NotFoundException {

  public SupermarketNotFoundException(SupermarketId id) {
    super("Supermarket not found with id: " + id);
  }
}
