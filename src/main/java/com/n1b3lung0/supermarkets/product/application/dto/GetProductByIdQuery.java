package com.n1b3lung0.supermarkets.product.application.dto;

import java.util.UUID;

/** Query to retrieve full product detail by internal UUID. */
public record GetProductByIdQuery(UUID productId) {}
