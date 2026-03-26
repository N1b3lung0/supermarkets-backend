package com.n1b3lung0.supermarkets.product.application.port.input.command;

import com.n1b3lung0.supermarkets.product.application.dto.DeactivateProductCommand;

/** Use case — soft-deactivates a product that is no longer sold. */
public interface DeactivateProductUseCase {

  void execute(DeactivateProductCommand command);
}
