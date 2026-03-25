package com.n1b3lung0.supermarkets.supermarket.domain.event;

/** Sealed interface for all Supermarket domain events. */
public sealed interface SupermarketEvent permits SupermarketRegistered {}
