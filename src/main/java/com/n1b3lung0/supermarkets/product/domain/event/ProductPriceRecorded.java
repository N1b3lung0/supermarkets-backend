package com.n1b3lung0.supermarkets.product.domain.event;

import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.product.domain.model.ProductPriceId;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.time.Instant;

/** Fired every time a price snapshot is recorded for a product. */
public record ProductPriceRecorded(
    ProductPriceId id, ProductId productId, Money unitPrice, Money bulkPrice, Instant recordedAt)
    implements ProductPriceEvent {}
