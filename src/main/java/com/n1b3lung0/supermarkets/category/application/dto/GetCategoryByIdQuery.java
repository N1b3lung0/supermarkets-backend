package com.n1b3lung0.supermarkets.category.application.dto;

import java.util.UUID;

/** Query to fetch a single Category by its internal UUID. */
public record GetCategoryByIdQuery(UUID id) {}
