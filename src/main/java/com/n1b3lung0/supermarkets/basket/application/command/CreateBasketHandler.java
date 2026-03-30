package com.n1b3lung0.supermarkets.basket.application.command;

import com.n1b3lung0.supermarkets.basket.application.dto.CreateBasketCommand;
import com.n1b3lung0.supermarkets.basket.application.port.input.command.CreateBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.model.Basket;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import java.util.Objects;

public class CreateBasketHandler implements CreateBasketUseCase {

  private final BasketRepositoryPort repository;

  public CreateBasketHandler(BasketRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public BasketId execute(CreateBasketCommand command) {
    Objects.requireNonNull(command, "command is required");
    var basket = Basket.create(command.name());
    repository.save(basket);
    return basket.getId();
  }
}
