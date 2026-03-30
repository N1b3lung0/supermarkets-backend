package com.n1b3lung0.supermarkets.basket.application.command;

import com.n1b3lung0.supermarkets.basket.application.dto.UpdateBasketItemQuantityCommand;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.UpdateBasketItemQuantityUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;
import java.util.Objects;

public class UpdateBasketItemQuantityHandler implements UpdateBasketItemQuantityUseCase {

  private final BasketRepositoryPort repository;

  public UpdateBasketItemQuantityHandler(BasketRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public void execute(UpdateBasketItemQuantityCommand command) {
    Objects.requireNonNull(command, "command is required");
    var basket =
        repository
            .findById(BasketId.of(command.basketId()))
            .orElseThrow(() -> new BasketNotFoundException(command.basketId().toString()));
    basket.updateItemQuantity(BasketItemId.of(command.itemId()), command.quantity());
    repository.save(basket);
  }
}
