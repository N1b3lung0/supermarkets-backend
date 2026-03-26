package com.n1b3lung0.supermarkets.product.application.port.input.command;

import com.n1b3lung0.supermarkets.product.application.dto.UpsertProductCommand;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;

/** Use case — creates or updates a product and records its current price snapshot. */
public interface UpsertProductUseCase {

  ProductId execute(UpsertProductCommand command);
}
