package com.n1b3lung0.supermarkets.basket.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Full basket comparison result. {@code cheapestSupermarketId} is null when no price data exists
 * for any item.
 */
public record BasketComparisonView(
    UUID basketId,
    String basketName,
    List<SupermarketBasketCost> perSupermarket,
    UUID cheapestSupermarketId,
    String cheapestSupermarketName) {}
