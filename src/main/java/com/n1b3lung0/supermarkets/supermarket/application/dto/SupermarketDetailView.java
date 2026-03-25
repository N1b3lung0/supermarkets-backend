package com.n1b3lung0.supermarkets.supermarket.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Read-side projection used by the get-by-id endpoint. */
public record SupermarketDetailView(UUID id, String name, String country, Instant createdAt) {}
