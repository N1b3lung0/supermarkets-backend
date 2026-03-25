package com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketDetailView;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketSummaryView;
import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketQueryPort;
import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketRepositoryPort;
import com.n1b3lung0.supermarkets.supermarket.domain.model.Supermarket;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.mapper.SupermarketPersistenceMapper;
import com.n1b3lung0.supermarkets.supermarket.infrastructure.adapter.output.persistence.repository.SpringSupermarketRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing both write (repository) and read (query) ports for Supermarket.
 * {@code @Transactional} is allowed here — infrastructure layer only.
 */
public class SupermarketJpaAdapter implements SupermarketRepositoryPort, SupermarketQueryPort {

  private final SpringSupermarketRepository repository;
  private final SupermarketPersistenceMapper mapper;

  public SupermarketJpaAdapter(
      SpringSupermarketRepository repository, SupermarketPersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  // --- SupermarketRepositoryPort ---

  @Override
  @Transactional
  public void save(Supermarket supermarket) {
    repository.save(mapper.toEntity(supermarket));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Supermarket> findById(SupermarketId id) {
    return repository.findActiveById(id.value()).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByName(SupermarketName name) {
    return repository.countByName(name.value()) > 0;
  }

  // --- SupermarketQueryPort ---

  @Override
  @Transactional(readOnly = true)
  public Optional<SupermarketDetailView> findDetailById(SupermarketId id) {
    return repository.findActiveById(id.value()).map(mapper::toDetailView);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<SupermarketSummaryView> findAll(Pageable pageable) {
    return repository.findAllActive(pageable).map(mapper::toSummaryView);
  }
}
