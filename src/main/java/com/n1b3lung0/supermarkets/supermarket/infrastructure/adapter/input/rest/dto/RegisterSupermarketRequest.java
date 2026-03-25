package com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request body for POST /api/v1/supermarkets. */
public record RegisterSupermarketRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "must be a valid ISO 3166-1 alpha-2 code")
        String country) {}
