package com.n1b3lung0.supermarkets.category.application.dto;

import java.util.UUID;

/** Compact summary view for paginated lists. */
public record CategorySummaryView(
    UUID id, String name, String externalId, String levelType, UUID supermarketId) {}
