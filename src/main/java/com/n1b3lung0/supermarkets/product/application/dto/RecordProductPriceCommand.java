package com.n1b3lung0.supermarkets.product.application.dto;

import com.n1b3lung0.supermarkets.product.domain.model.PriceInstructions;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;

/** Command to record a new price snapshot for a product. */
public record RecordProductPriceCommand(ProductId productId, PriceInstructions priceInstructions) {}
