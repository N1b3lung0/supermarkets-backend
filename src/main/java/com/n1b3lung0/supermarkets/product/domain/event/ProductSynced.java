package com.n1b3lung0.supermarkets.product.domain.event;

import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.time.Instant;

/** Fired when a product is created or updated via the scraper sync. */
public record ProductSynced(
    ProductId productId,
    ExternalProductId externalId,
    SupermarketId supermarketId,
    Instant occurredOn)
    implements ProductEvent {}
