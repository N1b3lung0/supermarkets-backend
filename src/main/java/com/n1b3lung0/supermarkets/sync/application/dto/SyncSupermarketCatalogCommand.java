package com.n1b3lung0.supermarkets.sync.application.dto;

import java.util.UUID;

/** Command that triggers a full catalog sync for one supermarket. */
public record SyncSupermarketCatalogCommand(UUID supermarketId) {}
