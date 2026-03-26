package com.n1b3lung0.supermarkets.product.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Full read-only view of a Product including latest price snapshot. */
public record ProductDetailView(
    UUID id,
    String externalId,
    UUID supermarketId,
    UUID categoryId,
    String name,
    String legalName,
    String description,
    String brand,
    String ean,
    String origin,
    String packaging,
    String thumbnailUrl,
    String storageInstructions,
    String usageInstructions,
    String mandatoryMentions,
    String productionVariant,
    String dangerMentions,
    String allergens,
    String ingredients,
    List<SupplierView> suppliers,
    boolean isWater,
    boolean requiresAgeCheck,
    boolean isBulk,
    boolean isVariableWeight,
    boolean isActive,
    int purchaseLimit,
    Instant createdAt,
    Instant updatedAt,
    ProductPriceView latestPrice) {}
