package com.n1b3lung0.supermarkets.category.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** HTTP request body for POST /api/v1/categories. */
public record RegisterCategoryRequest(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(max = 50) String externalId,
    @NotNull UUID supermarketId,
    @NotNull @Pattern(regexp = "TOP|SUB|LEAF", message = "levelType must be TOP, SUB or LEAF")
        String levelType,
    UUID parentId,
    @PositiveOrZero int order) {}
