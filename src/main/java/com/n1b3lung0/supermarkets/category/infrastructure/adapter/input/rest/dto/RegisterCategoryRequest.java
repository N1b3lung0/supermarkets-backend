package com.n1b3lung0.supermarkets.category.infrastructure.adapter.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** HTTP request body for POST /api/v1/categories. */
@Schema(description = "Payload to register a product category for a supermarket")
public record RegisterCategoryRequest(
    @Schema(description = "Human-readable category name", example = "Lácteos y huevos")
        @NotBlank
        @Size(max = 255)
        String name,
    @Schema(
            description = "Supermarket-assigned external identifier for this category",
            example = "lacteos")
        @NotBlank
        @Size(max = 50)
        String externalId,
    @Schema(
            description = "UUID of the supermarket that owns this category",
            example = "00000000-0000-0000-0000-000000000001")
        @NotNull
        UUID supermarketId,
    @Schema(
            description = "Hierarchy level: TOP (root), SUB (second level), LEAF (deepest level)",
            example = "TOP",
            allowableValues = {"TOP", "SUB", "LEAF"})
        @NotNull
        @Pattern(regexp = "TOP|SUB|LEAF", message = "levelType must be TOP, SUB or LEAF")
        String levelType,
    @Schema(
            description =
                "External ID of the parent category (required for SUB and LEAF levels,"
                    + " null for TOP)",
            example = "null",
            nullable = true)
        @Size(max = 50)
        String parentExternalId,
    @Schema(description = "Display sort order within the same hierarchy level", example = "1")
        @PositiveOrZero
        int order) {}
