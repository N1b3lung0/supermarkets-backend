package com.n1b3lung0.supermarkets.basket.domain.event;

/** Sealed base interface for all Basket domain events. */
public sealed interface BasketEvent permits BasketCreated, BasketItemAdded, BasketItemRemoved {}
