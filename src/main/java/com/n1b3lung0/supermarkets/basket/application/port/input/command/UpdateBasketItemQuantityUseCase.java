package com.n1b3lung0.supermarkets.basket.application.port.input.command;

import com.n1b3lung0.supermarkets.basket.application.dto.UpdateBasketItemQuantityCommand;

public interface UpdateBasketItemQuantityUseCase {
  void execute(UpdateBasketItemQuantityCommand command);
}
