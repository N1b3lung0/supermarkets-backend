package com.n1b3lung0.supermarkets.comparison.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Read-only view of a single product match returned by the comparison query. All monetary fields
 * are raw BigDecimal (already in EUR) to avoid serialisation complexity with Money.
 */
public record ProductMatchView(
    UUID productId,
    UUID supermarketId,
    String supermarketName,
    String productName,
    BigDecimal unitPrice,
    BigDecimal bulkPrice,
    BigDecimal referencePrice,
    String referenceFormat,
    Instant priceRecordedAt) {}
