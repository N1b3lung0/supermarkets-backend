package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.product.application.dto.ProductPriceView;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductPriceQueryPort;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductPriceRepositoryPort;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.product.domain.model.ProductPrice;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper.ProductPricePersistenceMapper;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository.SpringProductPriceRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter implementing both write and read ports for ProductPrice. */
public class ProductPriceJpaAdapter implements ProductPriceRepositoryPort, ProductPriceQueryPort {

  private final SpringProductPriceRepository repository;
  private final ProductPricePersistenceMapper mapper;

  public ProductPriceJpaAdapter(
      SpringProductPriceRepository repository, ProductPricePersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public void save(ProductPrice productPrice) {
    repository.save(mapper.toEntity(productPrice));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ProductPrice> findLatestByProductId(ProductId productId) {
    return repository.findLatestByProductId(productId.value()).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductPriceView> findHistoryByProductId(ProductId productId, Pageable pageable) {
    return repository.findHistoryByProductId(productId.value(), pageable).map(mapper::toView);
  }
}
