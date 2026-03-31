package com.n1b3lung0.supermarkets.basket.application.command;

import com.n1b3lung0.supermarkets.basket.application.dto.ClearBasketCommand;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.ClearBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import java.util.Objects;

public class ClearBasketHandler implements ClearBasketUseCase {

  private final BasketRepositoryPort repository;

  public ClearBasketHandler(BasketRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public void execute(ClearBasketCommand command) {
    Objects.requireNonNull(command, "command is required");
    var basket =
        repository
            .findById(BasketId.of(command.basketId()))
            .orElseThrow(() -> new BasketNotFoundException(command.basketId().toString()));
    basket.clear();
    repository.save(basket);
  }
}
