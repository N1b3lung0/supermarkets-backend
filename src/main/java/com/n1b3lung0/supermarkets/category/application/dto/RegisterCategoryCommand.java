package com.n1b3lung0.supermarkets.category.application.dto;

import java.util.UUID;

/**
 * Command to register a new Category.
 *
 * <p>{@code parentExternalId} is the external (supermarket-assigned) ID of the parent category.
 * Required for SUB and LEAF levels; must be {@code null} for TOP. The handler resolves the internal
 * {@link com.n1b3lung0.supermarkets.category.domain.model.CategoryId} via the repository.
 */
public record RegisterCategoryCommand(
    String name,
    String externalId,
    UUID supermarketId,
    String levelType,
    String parentExternalId,
    int order) {}
