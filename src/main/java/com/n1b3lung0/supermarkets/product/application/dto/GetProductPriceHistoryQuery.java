package com.n1b3lung0.supermarkets.product.application.dto;

import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** Query to retrieve the full price history for a product (paginated, newest first). */
public record GetProductPriceHistoryQuery(UUID productId, Pageable pageable) {}
