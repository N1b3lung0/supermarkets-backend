package com.n1b3lung0.supermarkets.product.application.dto;

import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.time.Instant;
import java.util.UUID;

/** Read-only view of a single ProductPrice history entry. */
public record ProductPriceView(
    UUID id,
    UUID productId,
    Money unitPrice,
    Money bulkPrice,
    Money referencePrice,
    String referenceFormat,
    Integer iva,
    boolean priceDecreased,
    Money previousUnitPrice,
    Instant recordedAt) {}
