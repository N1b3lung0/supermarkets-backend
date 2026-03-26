package com.n1b3lung0.supermarkets.category.application.dto;

import java.util.UUID;

/** Command to register a new Category. */
public record RegisterCategoryCommand(
    String name,
    String externalId,
    UUID supermarketId,
    String levelType,
    UUID parentId,
    int order) {}
