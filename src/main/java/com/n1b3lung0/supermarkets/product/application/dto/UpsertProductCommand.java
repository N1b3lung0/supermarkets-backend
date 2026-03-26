package com.n1b3lung0.supermarkets.product.application.dto;

import com.n1b3lung0.supermarkets.product.domain.model.PriceInstructions;
import java.util.List;
import java.util.UUID;

/**
 * Command record for the UpsertProduct use case. Mirrors all mutable product fields plus the price
 * instructions block — populated by the scraper from the API JSON.
 */
public record UpsertProductCommand(
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
    List<String> supplierNames,
    boolean isWater,
    boolean requiresAgeCheck,
    boolean isBulk,
    boolean isVariableWeight,
    int purchaseLimit,
    PriceInstructions priceInstructions) {}
