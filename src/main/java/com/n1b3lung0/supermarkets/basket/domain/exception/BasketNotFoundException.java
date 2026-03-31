package com.n1b3lung0.supermarkets.basket.domain.exception;

import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;

public class BasketNotFoundException extends NotFoundException {
  public BasketNotFoundException(String id) {
    super("Basket not found: " + id);
  }
}
