package com.n1b3lung0.supermarkets.basket.application.command;

import com.n1b3lung0.supermarkets.basket.application.dto.RemoveBasketItemCommand;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.RemoveBasketItemUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;
import java.util.Objects;

public class RemoveBasketItemHandler implements RemoveBasketItemUseCase {

  private final BasketRepositoryPort repository;

  public RemoveBasketItemHandler(BasketRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public void execute(RemoveBasketItemCommand command) {
    Objects.requireNonNull(command, "command is required");
    var basket =
        repository
            .findById(BasketId.of(command.basketId()))
            .orElseThrow(() -> new BasketNotFoundException(command.basketId().toString()));
    basket.removeItem(BasketItemId.of(command.itemId()));
    repository.save(basket);
  }
}
