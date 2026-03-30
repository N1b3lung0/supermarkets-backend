package com.n1b3lung0.supermarkets.basket.domain.exception;

import com.n1b3lung0.supermarkets.shared.domain.exception.ConflictException;

public class DuplicateBasketItemException extends ConflictException {
  public DuplicateBasketItemException(String productName) {
    super("Basket already contains an item with product name: " + productName);
  }
}
