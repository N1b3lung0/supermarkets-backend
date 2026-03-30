package com.n1b3lung0.supermarkets.basket.application.port.input.command;

import com.n1b3lung0.supermarkets.basket.application.dto.CreateBasketCommand;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;

public interface CreateBasketUseCase {
  BasketId execute(CreateBasketCommand command);
}
