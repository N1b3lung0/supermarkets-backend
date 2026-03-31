package com.n1b3lung0.supermarkets.basket.application.port.input.command;

import com.n1b3lung0.supermarkets.basket.application.dto.RemoveBasketItemCommand;

public interface RemoveBasketItemUseCase {
  void execute(RemoveBasketItemCommand command);
}
