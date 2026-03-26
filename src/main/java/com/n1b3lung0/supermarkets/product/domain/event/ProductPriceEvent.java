package com.n1b3lung0.supermarkets.product.domain.event;

/** Sealed marker interface for all ProductPrice domain events. */
public sealed interface ProductPriceEvent permits ProductPriceRecorded {}
