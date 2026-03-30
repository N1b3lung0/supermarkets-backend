package com.n1b3lung0.supermarkets.basket.application.query;

import com.n1b3lung0.supermarkets.basket.application.dto.BasketDetailView;
import com.n1b3lung0.supermarkets.basket.application.dto.BasketItemView;
import com.n1b3lung0.supermarkets.basket.application.dto.GetBasketByIdQuery;
import com.n1b3lung0.supermarkets.basket.application.port.input.query.GetBasketByIdUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import java.util.Objects;

public class GetBasketByIdHandler implements GetBasketByIdUseCase {

  private final BasketRepositoryPort repository;

  public GetBasketByIdHandler(BasketRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public BasketDetailView execute(GetBasketByIdQuery query) {
    Objects.requireNonNull(query, "query is required");
    var basket =
        repository
            .findById(BasketId.of(query.basketId()))
            .orElseThrow(() -> new BasketNotFoundException(query.basketId().toString()));

    var items =
        basket.getItems().stream()
            .map(i -> new BasketItemView(i.getId().value(), i.getProductName(), i.getQuantity()))
            .toList();

    return new BasketDetailView(basket.getId().value(), basket.getName(), items);
  }
}
