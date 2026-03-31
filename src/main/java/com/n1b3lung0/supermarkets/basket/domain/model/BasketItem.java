package com.n1b3lung0.supermarkets.basket.domain.model;

import java.util.Objects;

/**
 * Entity representing a single line item in a Basket. Mutable within the aggregate boundary —
 * quantity can be updated by the Basket aggregate root.
 */
public class BasketItem {

  private final BasketItemId id;
  private final String productName;
  private int quantity;

  private BasketItem(BasketItemId id, String productName, int quantity) {
    this.id = id;
    this.productName = productName;
    this.quantity = quantity;
  }

  public static BasketItem create(String productName, int quantity) {
    Objects.requireNonNull(productName, "productName is required");
    if (productName.isBlank()) {
      throw new IllegalArgumentException("productName must not be blank");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be > 0, got: " + quantity);
    }
    return new BasketItem(BasketItemId.generate(), productName, quantity);
  }

  /** Public reconstitute for persistence mapper use. */
  public static BasketItem reconstitute(BasketItemId id, String productName, int quantity) {
    return new BasketItem(id, productName, quantity);
  }

  void updateQuantity(int newQuantity) {
    if (newQuantity <= 0) {
      throw new IllegalArgumentException("quantity must be > 0, got: " + newQuantity);
    }
    this.quantity = newQuantity;
  }

  public BasketItemId getId() {
    return id;
  }

  public String getProductName() {
    return productName;
  }

  public int getQuantity() {
    return quantity;
  }
}
