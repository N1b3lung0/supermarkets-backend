package com.n1b3lung0.supermarkets.product.application.port.input.command;

import com.n1b3lung0.supermarkets.product.application.dto.RecordProductPriceCommand;

/** Use case — appends a new immutable price snapshot for a product. */
public interface RecordProductPriceUseCase {

  void execute(RecordProductPriceCommand command);
}
