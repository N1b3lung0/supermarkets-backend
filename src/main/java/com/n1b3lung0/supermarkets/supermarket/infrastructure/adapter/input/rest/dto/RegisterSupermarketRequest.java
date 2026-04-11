package com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.input.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request body for POST /api/v1/supermarkets. */
@Schema(description = "Payload to register a new supermarket chain")
public record RegisterSupermarketRequest(
    @Schema(description = "Display name of the supermarket chain", example = "Mercadona")
        @NotBlank
        @Size(max = 100)
        String name,
    @Schema(
            description = "ISO 3166-1 alpha-2 country code where the chain operates",
            example = "ES")
        @NotBlank
        @Pattern(regexp = "[A-Z]{2}", message = "must be a valid ISO 3166-1 alpha-2 code")
        String country) {}
