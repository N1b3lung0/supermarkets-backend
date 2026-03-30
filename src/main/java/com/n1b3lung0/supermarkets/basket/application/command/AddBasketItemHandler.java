package com.n1b3lung0.supermarkets.basket.application.command;

import com.n1b3lung0.supermarkets.basket.application.dto.AddBasketItemCommand;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.AddBasketItemUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;
import java.util.Objects;

public class AddBasketItemHandler implements AddBasketItemUseCase {

  private final BasketRepositoryPort repository;

  public AddBasketItemHandler(BasketRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public BasketItemId execute(AddBasketItemCommand command) {
    Objects.requireNonNull(command, "command is required");
    var basket =
        repository
            .findById(BasketId.of(command.basketId()))
            .orElseThrow(() -> new BasketNotFoundException(command.basketId().toString()));
    var item = basket.addItem(command.productName(), command.quantity());
    repository.save(basket);
    return item.getId();
  }
}
