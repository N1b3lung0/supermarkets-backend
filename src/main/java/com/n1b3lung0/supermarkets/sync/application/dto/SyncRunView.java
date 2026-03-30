package com.n1b3lung0.supermarkets.sync.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Read-side projection for a SyncRun. */
public record SyncRunView(
    UUID id,
    UUID supermarketId,
    Instant startedAt,
    Instant finishedAt,
    String status,
    int categoriesSynced,
    int productsSynced,
    int productsDeactivated,
    String errorMessage) {}
