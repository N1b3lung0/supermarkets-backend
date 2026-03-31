package com.n1b3lung0.supermarkets.basket.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Per-supermarket cost breakdown for a basket comparison. */
public record SupermarketBasketCost(
    UUID supermarketId,
    String supermarketName,
    BigDecimal totalCost,
    List<BasketItemMatchView> itemMatches) {}
