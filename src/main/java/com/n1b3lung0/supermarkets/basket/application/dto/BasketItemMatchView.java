package com.n1b3lung0.supermarkets.basket.application.dto;

import java.math.BigDecimal;

/** A single item's best matching product price at a given supermarket. */
public record BasketItemMatchView(
    String basketItemName,
    int quantity,
    String matchedProductName,
    BigDecimal unitPrice,
    BigDecimal lineTotal) {}
