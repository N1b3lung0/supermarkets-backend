package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;
import com.n1b3lung0.supermarkets.product.application.dto.ProductSummaryView;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductQueryPort;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductRepositoryPort;
import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.product.domain.model.Product;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper.ProductPersistenceMapper;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper.ProductPricePersistenceMapper;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository.SpringProductPriceRepository;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository.SpringProductRepository;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter implementing both write and read ports for Product. */
public class ProductJpaAdapter implements ProductRepositoryPort, ProductQueryPort {

  private final SpringProductRepository productRepository;
  private final SpringProductPriceRepository priceRepository;
  private final ProductPersistenceMapper mapper;
  private final ProductPricePersistenceMapper priceMapper;

  public ProductJpaAdapter(
      SpringProductRepository productRepository,
      SpringProductPriceRepository priceRepository,
      ProductPersistenceMapper mapper,
      ProductPricePersistenceMapper priceMapper) {
    this.productRepository = productRepository;
    this.priceRepository = priceRepository;
    this.mapper = mapper;
    this.priceMapper = priceMapper;
  }

  // -------------------------------------------------------------------------
  // ProductRepositoryPort
  // -------------------------------------------------------------------------

  @Override
  @Transactional
  public void save(Product product) {
    productRepository.save(mapper.toEntity(product));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findById(ProductId id) {
    return productRepository.findActiveById(id.value()).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> findByExternalIdAndSupermarket(
      ExternalProductId externalId, SupermarketId supermarketId) {
    return productRepository
        .findByExternalIdAndSupermarketId(externalId.value(), supermarketId.value())
        .map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> findActiveExternalIdsBySupermarket(SupermarketId supermarketId) {
    return productRepository.findActiveExternalIdsBySupermarketId(supermarketId.value());
  }

  @Override
  @Transactional
  public void deleteById(ProductId id) {
    productRepository
        .findActiveById(id.value())
        .ifPresent(
            entity -> {
              entity.setDeletedAt(java.time.Instant.now());
              productRepository.save(entity);
            });
  }

  // -------------------------------------------------------------------------
  // ProductQueryPort
  // -------------------------------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public Optional<ProductDetailView> findDetailById(ProductId id) {
    return productRepository
        .findActiveById(id.value())
        .map(
            entity -> {
              var latestPrice = priceRepository.findLatestByProductId(entity.getId()).orElse(null);
              return mapper.toDetailView(entity, latestPrice);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductSummaryView> findSummariesByCategory(
      CategoryId categoryId, Pageable pageable) {
    return productRepository
        .findByCategoryId(categoryId.value(), pageable)
        .map(
            entity -> {
              var latestPrice = priceRepository.findLatestByProductId(entity.getId()).orElse(null);
              return mapper.toSummaryViewWithPrice(entity, latestPrice);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductSummaryView> findSummariesBySupermarket(
      SupermarketId supermarketId, Pageable pageable) {
    return productRepository
        .findBySupermarketId(supermarketId.value(), pageable)
        .map(
            entity -> {
              var latestPrice = priceRepository.findLatestByProductId(entity.getId()).orElse(null);
              return mapper.toSummaryViewWithPrice(entity, latestPrice);
            });
  }
}
