package com.n1b3lung0.supermarkets.category.domain.event;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryName;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.time.Instant;

/** Emitted when a new Category is registered in the system. */
public record CategoryRegistered(
    CategoryId categoryId, CategoryName name, SupermarketId supermarketId, Instant occurredOn)
    implements CategoryEvent {}
