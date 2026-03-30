package com.n1b3lung0.supermarkets.basket.application.port.input.query;

import com.n1b3lung0.supermarkets.basket.application.dto.BasketDetailView;
import com.n1b3lung0.supermarkets.basket.application.dto.GetBasketByIdQuery;

public interface GetBasketByIdUseCase {
  BasketDetailView execute(GetBasketByIdQuery query);
}
