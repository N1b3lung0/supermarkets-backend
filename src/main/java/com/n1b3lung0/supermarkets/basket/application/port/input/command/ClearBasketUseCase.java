package com.n1b3lung0.supermarkets.basket.application.port.input.command;

import com.n1b3lung0.supermarkets.basket.application.dto.ClearBasketCommand;

public interface ClearBasketUseCase {
  void execute(ClearBasketCommand command);
}
