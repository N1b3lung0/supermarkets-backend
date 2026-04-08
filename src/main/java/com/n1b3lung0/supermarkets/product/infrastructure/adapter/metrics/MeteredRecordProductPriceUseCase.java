package com.n1b3lung0.supermarkets.product.infrastructure.adapter.metrics;

import com.n1b3lung0.supermarkets.product.application.dto.RecordProductPriceCommand;
import com.n1b3lung0.supermarkets.product.application.port.input.command.RecordProductPriceUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Objects;

/**
 * Infrastructure decorator that adds a {@code prices.recorded.total} counter to {@link
 * RecordProductPriceUseCase}.
 *
 * <p>Keeping metrics logic in infrastructure ensures the application handler stays annotation-free,
 * consistent with the project's hexagonal architecture rules.
 */
public class MeteredRecordProductPriceUseCase implements RecordProductPriceUseCase {

  private final RecordProductPriceUseCase delegate;
  private final MeterRegistry meterRegistry;

  public MeteredRecordProductPriceUseCase(
      RecordProductPriceUseCase delegate, MeterRegistry meterRegistry) {
    this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry is required");
  }

  @Override
  public void execute(RecordProductPriceCommand command) {
    Objects.requireNonNull(command, "command is required");
    delegate.execute(command);
    meterRegistry.counter("prices.recorded.total").increment();
  }
}
