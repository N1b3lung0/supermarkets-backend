package com.n1b3lung0.supermarkets.basket.domain.exception;

import com.n1b3lung0.supermarkets.shared.domain.exception.NotFoundException;

public class BasketItemNotFoundException extends NotFoundException {
  public BasketItemNotFoundException(String itemId) {
    super("Basket item not found: " + itemId);
  }
}
