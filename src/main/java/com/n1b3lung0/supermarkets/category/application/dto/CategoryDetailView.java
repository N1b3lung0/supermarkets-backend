package com.n1b3lung0.supermarkets.category.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Full detail view for a single Category (used in GET /categories/{id}). */
public record CategoryDetailView(
    UUID id,
    String name,
    String externalId,
    UUID supermarketId,
    String levelType,
    UUID parentId,
    int order,
    Instant createdAt) {}
