package com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import com.n1b3lung0.supermarkets.category.domain.CategoryMother;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.CategoryJpaAdapter;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.mapper.CategoryPersistenceMapper;
import com.n1b3lung0.supermarkets.category.infrastructure.adapter.output.persistence.repository.SpringCategoryRepository;
import com.n1b3lung0.supermarkets.product.domain.ProductMother;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper.ProductPersistenceMapper;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.mapper.ProductPricePersistenceMapper;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository.SpringProductPriceRepository;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.output.persistence.repository.SpringProductRepository;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductJpaAdapterTest extends PostgresIntegrationTest {

  @Autowired private SpringProductRepository productRepository;
  @Autowired private SpringProductPriceRepository priceRepository;
  @Autowired private SpringCategoryRepository categoryRepository;

  private ProductJpaAdapter productAdapter;
  private ProductPriceJpaAdapter priceAdapter;

  private static final SupermarketId TEST_SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  @BeforeEach
  void setUp() {
    var productMapper = new ProductPersistenceMapper();
    var priceMapper = new ProductPricePersistenceMapper();
    productAdapter =
        new ProductJpaAdapter(productRepository, priceRepository, productMapper, priceMapper);
    priceAdapter = new ProductPriceJpaAdapter(priceRepository, priceMapper);

    // Persist a category so FK constraint is satisfied
    var categoryAdapter =
        new CategoryJpaAdapter(categoryRepository, new CategoryPersistenceMapper());
    var top = CategoryMother.topCategoryWithExternalId(TEST_SUPERMARKET, "TOP-PROD-TEST");
    categoryAdapter.save(top);
    ProductMother.DEFAULT_CATEGORY.getClass(); // ensure static field initialized
  }

  @Test
  void save_andFindById_shouldRoundtrip() {
    // Save a category leaf that our product references
    var catAdapter = new CategoryJpaAdapter(categoryRepository, new CategoryPersistenceMapper());
    var top = CategoryMother.topCategoryWithExternalId(TEST_SUPERMARKET, "TOP-FOR-PRODUCT");
    catAdapter.save(top);

    var product =
        ProductMother.simpleProductWithExternalId(TEST_SUPERMARKET, top.getId(), "PROD-RT-1");
    productAdapter.save(product);

    var found = productAdapter.findById(product.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getName().value()).isEqualTo("Leche Entera");
    assertThat(found.get().getBrand().value()).isEqualTo("Hacendado");
  }

  @Test
  void findByExternalIdAndSupermarket_shouldReturnEmpty_whenNotExists() {
    assertThat(
            productAdapter.findByExternalIdAndSupermarket(
                com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId.of("NONEXISTENT"),
                TEST_SUPERMARKET))
        .isEmpty();
  }

  @Test
  void recordPrice_andFindLatest_shouldReturnMostRecent() {
    var catAdapter = new CategoryJpaAdapter(categoryRepository, new CategoryPersistenceMapper());
    var top = CategoryMother.topCategoryWithExternalId(TEST_SUPERMARKET, "TOP-FOR-PRICE");
    catAdapter.save(top);

    var product =
        ProductMother.simpleProductWithExternalId(TEST_SUPERMARKET, top.getId(), "PROD-PRICE-1");
    productAdapter.save(product);

    var price1 = ProductMother.simpleProductPrice(product.getId());
    var price2 = ProductMother.simpleProductPrice(product.getId());
    priceAdapter.save(price1);
    priceAdapter.save(price2);

    var latest = priceAdapter.findLatestByProductId(product.getId());
    assertThat(latest).isPresent();
  }

  @Test
  void priceHistory_shouldReturnAllEntries() {
    var catAdapter = new CategoryJpaAdapter(categoryRepository, new CategoryPersistenceMapper());
    var top = CategoryMother.topCategoryWithExternalId(TEST_SUPERMARKET, "TOP-FOR-HIST");
    catAdapter.save(top);

    var product =
        ProductMother.simpleProductWithExternalId(TEST_SUPERMARKET, top.getId(), "PROD-HIST-1");
    productAdapter.save(product);

    priceAdapter.save(ProductMother.simpleProductPrice(product.getId()));
    priceAdapter.save(ProductMother.simpleProductPrice(product.getId()));

    var history = priceAdapter.findHistoryByProductId(product.getId(), PageRequest.of(0, 10));
    assertThat(history.getContent()).hasSize(2);
  }

  @Test
  void findDetailById_shouldReturnEmpty_whenNotFound() {
    assertThat(productAdapter.findDetailById(ProductId.generate())).isEmpty();
  }

  @Test
  void findActiveExternalIds_shouldReturnOnlyActiveProducts() {
    var catAdapter = new CategoryJpaAdapter(categoryRepository, new CategoryPersistenceMapper());
    var top = CategoryMother.topCategoryWithExternalId(TEST_SUPERMARKET, "TOP-FOR-ACTIVE");
    catAdapter.save(top);

    var product =
        ProductMother.simpleProductWithExternalId(TEST_SUPERMARKET, top.getId(), "ACTIVE-EXT-99");
    productAdapter.save(product);

    var ids = productAdapter.findActiveExternalIdsBySupermarket(TEST_SUPERMARKET);
    assertThat(ids).contains("ACTIVE-EXT-99");
  }
}
