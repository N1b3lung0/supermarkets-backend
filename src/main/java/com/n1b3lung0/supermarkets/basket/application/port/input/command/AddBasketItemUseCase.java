package com.n1b3lung0.supermarkets.basket.application.port.input.command;

import com.n1b3lung0.supermarkets.basket.application.dto.AddBasketItemCommand;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;

public interface AddBasketItemUseCase {
  BasketItemId execute(AddBasketItemCommand command);
}
