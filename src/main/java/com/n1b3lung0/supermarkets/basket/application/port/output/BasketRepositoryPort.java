package com.n1b3lung0.supermarkets.basket.application.port.output;

import com.n1b3lung0.supermarkets.basket.domain.model.Basket;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import java.util.Optional;

public interface BasketRepositoryPort {
  void save(Basket basket);

  Optional<Basket> findById(BasketId id);
}
