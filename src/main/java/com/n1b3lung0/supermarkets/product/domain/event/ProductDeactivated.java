package com.n1b3lung0.supermarkets.product.domain.event;

import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.time.Instant;

/** Fired when a product is soft-deactivated (e.g. removed from the supermarket catalogue). */
public record ProductDeactivated(
    ProductId productId, SupermarketId supermarketId, Instant occurredOn) implements ProductEvent {}
