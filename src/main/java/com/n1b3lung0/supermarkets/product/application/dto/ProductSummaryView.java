package com.n1b3lung0.supermarkets.product.application.dto;

import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.util.UUID;

/** Lightweight read-only view of a Product — used in paginated list responses. */
public record ProductSummaryView(
    UUID id,
    String externalId,
    String name,
    String brand,
    String thumbnailUrl,
    Money unitPrice,
    Money bulkPrice,
    boolean isActive,
    UUID supermarketId,
    UUID categoryId) {}
