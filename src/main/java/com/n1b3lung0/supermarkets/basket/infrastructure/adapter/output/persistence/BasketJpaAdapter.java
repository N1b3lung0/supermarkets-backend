package com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.model.Basket;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.mapper.BasketPersistenceMapper;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.repository.SpringBasketRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public class BasketJpaAdapter implements BasketRepositoryPort {

  private final SpringBasketRepository repository;
  private final BasketPersistenceMapper mapper;

  public BasketJpaAdapter(SpringBasketRepository repository, BasketPersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public void save(Basket basket) {
    repository.save(mapper.toEntity(basket));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Basket> findById(BasketId id) {
    return repository.findByIdWithItems(id.value()).map(mapper::toDomain);
  }
}
