package com.n1b3lung0.supermarkets.product.application.command;

import com.n1b3lung0.supermarkets.product.application.dto.DeactivateProductCommand;
import com.n1b3lung0.supermarkets.product.application.port.input.command.DeactivateProductUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
import com.n1b3lung0.supermarkets.product.domain.exception.ProductNotFoundException;
import java.util.Objects;

/**
 * Soft-deactivates a product by calling {@link
 * com.n1b3lung0.supermarkets.product.domain.model.Product#deactivate()}.
 */
public class DeactivateProductHandler implements DeactivateProductUseCase {

  private final ProductRepositoryPort repository;

  public DeactivateProductHandler(ProductRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public void execute(DeactivateProductCommand command) {
    Objects.requireNonNull(command, "command is required");

    var product =
        repository
            .findById(command.productId())
            .orElseThrow(() -> new ProductNotFoundException(command.productId()));

    product.deactivate();

    // Save only if deactivate() actually changed state (emitted event)
    if (!product.pullDomainEvents().isEmpty()) {
      repository.save(product);
    }
  }
}
