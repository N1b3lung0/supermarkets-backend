package com.n1b3lung0.supermarkets.category.domain.event;

/** Sealed marker for all Category domain events. */
public sealed interface CategoryEvent permits CategoryRegistered {}
