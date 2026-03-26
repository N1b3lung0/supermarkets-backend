package com.n1b3lung0.supermarkets.product.domain.event;

/** Sealed marker interface for all Product domain events. */
public sealed interface ProductEvent permits ProductSynced, ProductDeactivated {}
