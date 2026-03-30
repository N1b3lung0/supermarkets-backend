package com.n1b3lung0.supermarkets.comparison.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Top-level comparison result view returned by the REST API. Matches are already sorted by unit
 * price ascending. {@code cheapestSupermarketId} is null when the match list is empty.
 */
public record ProductComparisonView(
    String searchTerm,
    List<ProductMatchView> matches,
    UUID cheapestSupermarketId,
    String cheapestSupermarketName) {}
