package com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.category.application.dto.CategoryDetailView;
import com.n1b3lung0.supermarkets.category.application.dto.CategorySummaryView;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryQueryPort;
import com.n1b3lung0.supermarkets.category.application.port.output.CategoryRepositoryPort;
import com.n1b3lung0.supermarkets.category.domain.model.Category;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.mapper.CategoryPersistenceMapper;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.repository.SpringCategoryRepository;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter implementing both write and read ports for Category. */
public class CategoryJpaAdapter implements CategoryRepositoryPort, CategoryQueryPort {

  private final SpringCategoryRepository repository;
  private final CategoryPersistenceMapper mapper;

  public CategoryJpaAdapter(SpringCategoryRepository repository, CategoryPersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  // -------------------------------------------------------------------------
  // CategoryRepositoryPort
  // -------------------------------------------------------------------------

  @Override
  @Transactional
  public void save(Category category) {
    repository.save(mapper.toEntity(category));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Category> findById(CategoryId id) {
    return repository.findActiveById(id.value()).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Category> findByExternalIdAndSupermarketId(
      ExternalCategoryId externalId, SupermarketId supermarketId) {
    return repository
        .findByExternalIdAndSupermarketId(externalId.value(), supermarketId.value())
        .map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByExternalIdAndSupermarketId(
      ExternalCategoryId externalId, SupermarketId supermarketId) {
    return repository.countByExternalIdAndSupermarketId(externalId.value(), supermarketId.value())
        > 0;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Category> findByLevelTypeAndSupermarketId(
      String levelType, SupermarketId supermarketId) {
    return repository.findByLevelTypeAndSupermarketId(levelType, supermarketId.value()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  // -------------------------------------------------------------------------
  // CategoryQueryPort
  // -------------------------------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public Optional<CategoryDetailView> findDetailById(CategoryId id) {
    return repository.findActiveById(id.value()).map(mapper::toDetailView);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CategorySummaryView> findAll(
      UUID supermarketId, String levelType, Pageable pageable) {
    return repository.findAllActive(supermarketId, levelType, pageable).map(mapper::toSummaryView);
  }
}
