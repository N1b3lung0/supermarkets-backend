package com.n1b3lung0.supermarkets.product.application.command;

import com.n1b3lung0.supermarkets.product.application.dto.RecordProductPriceCommand;
import com.n1b3lung0.supermarkets.product.application.port.input.command.RecordProductPriceUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductPriceRepositoryPort;
import com.n1b3lung0.supermarkets.product.domain.model.ProductPrice;
import java.util.Objects;

/** Appends a new immutable price snapshot — never updates an existing row. */
public class RecordProductPriceHandler implements RecordProductPriceUseCase {

  private final ProductPriceRepositoryPort repository;

  public RecordProductPriceHandler(ProductPriceRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public void execute(RecordProductPriceCommand command) {
    Objects.requireNonNull(command, "command is required");
    var price = ProductPrice.create(command.productId(), command.priceInstructions());
    repository.save(price);
  }
}
