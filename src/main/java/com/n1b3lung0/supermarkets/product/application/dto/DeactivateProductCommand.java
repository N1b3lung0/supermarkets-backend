package com.n1b3lung0.supermarkets.product.application.dto;

import com.n1b3lung0.supermarkets.product.domain.model.ProductId;

/** Command to soft-deactivate a product that is no longer sold. */
public record DeactivateProductCommand(ProductId productId) {}
