package com.n1b3lung0.supermarkets.supermarket.application.dto;

import java.util.UUID;

/** Read-side projection used by list/summary endpoints. */
public record SupermarketSummaryView(UUID id, String name, String country) {}
