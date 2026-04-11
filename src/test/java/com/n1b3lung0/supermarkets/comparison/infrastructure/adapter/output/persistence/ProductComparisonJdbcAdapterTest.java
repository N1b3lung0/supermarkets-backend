package com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.output.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

/**
 * Step 65 — integration test for ProductComparisonJdbcAdapter using Testcontainers PostgreSQL.
 * Seeds supermarket + category + product + price data directly via SQL for full control.
 *
 * <p>NOTE: Not annotated with {@code @Transactional} because {@code REFRESH MATERIALIZED VIEW} only
 * sees committed data. Cleanup is handled explicitly in {@code @AfterEach}.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductComparisonJdbcAdapterTest extends PostgresIntegrationTest {

  @Autowired private JdbcClient jdbcClient;

  private ProductComparisonJdbcAdapter adapter;

  private UUID mercadonaId;
  private UUID lidlId;
  private UUID categoryId;

  private final List<UUID> insertedSupermarkets = new ArrayList<>();
  private final List<UUID> insertedCategories = new ArrayList<>();
  private final List<UUID> insertedProducts = new ArrayList<>();

  @BeforeEach
  void setUp() {
    adapter = new ProductComparisonJdbcAdapter(jdbcClient);

    mercadonaId = insertSupermarket("Mercadona Test");
    lidlId = insertSupermarket("LIDL Test");
    categoryId = insertCategory(mercadonaId);

    var aceite1 = insertProduct(mercadonaId, categoryId, "Aceite oliva virgen extra 1L", "EXT-1");
    var aceite2 = insertProduct(lidlId, categoryId, "Aceite oliva virgen extra 750ml", "EXT-2");
    var leche = insertProduct(mercadonaId, categoryId, "Leche entera 1L", "EXT-3");

    insertPrice(aceite1, 3.50);
    insertPrice(aceite2, 2.99);
    insertPrice(leche, 0.85);

    // Data is now committed — refresh MV so comparison queries see it
    jdbcClient.sql("REFRESH MATERIALIZED VIEW latest_product_prices").update();
  }

  @AfterEach
  void tearDown() {
    if (!insertedProducts.isEmpty()) {
      for (UUID id : insertedProducts) {
        jdbcClient
            .sql("DELETE FROM product_prices WHERE product_id = :id")
            .param("id", id)
            .update();
      }
      for (UUID id : insertedProducts) {
        jdbcClient.sql("DELETE FROM products WHERE id = :id").param("id", id).update();
      }
    }
    for (UUID id : insertedCategories) {
      jdbcClient.sql("DELETE FROM categories WHERE id = :id").param("id", id).update();
    }
    for (UUID id : insertedSupermarkets) {
      jdbcClient.sql("DELETE FROM supermarkets WHERE id = :id").param("id", id).update();
    }
    jdbcClient.sql("REFRESH MATERIALIZED VIEW latest_product_prices").update();

    insertedProducts.clear();
    insertedCategories.clear();
    insertedSupermarkets.clear();
  }

  @Test
  void findMatchesByName_searchAceite_returnsProductsFromAllSupermarkets() {
    // Scope to test-inserted supermarkets so pre-seeded demo data does not affect the count
    var results = adapter.findMatchesByName("aceite", List.of(mercadonaId, lidlId));

    assertThat(results).hasSize(2);
    // sorted by price ASC
    assertThat(results.get(0).unitPriceAmount()).isLessThan(results.get(1).unitPriceAmount());
  }

  @Test
  void findMatchesByName_filterBySupermarket_returnsOnlyThatSupermarket() {
    var results = adapter.findMatchesByName("aceite", List.of(mercadonaId));

    assertThat(results).hasSize(1);
    assertThat(results.get(0).supermarketId()).isEqualTo(mercadonaId);
  }

  @Test
  void findMatchesByName_noResults_returnsEmptyList() {
    var results = adapter.findMatchesByName("patatas fritas sabor jamón", List.of());
    assertThat(results).isEmpty();
  }

  @Test
  void findMatchesByName_caseInsensitive_matchesRegardlessOfCase() {
    // Scope to test-inserted supermarkets so pre-seeded demo data does not affect the count
    var lower = adapter.findMatchesByName("aceite", List.of(mercadonaId, lidlId));
    var upper = adapter.findMatchesByName("ACEITE", List.of(mercadonaId, lidlId));
    assertThat(lower).hasSameSizeAs(upper);
  }

  // --- helpers ---

  private UUID insertSupermarket(String name) {
    var id = UUID.randomUUID();
    jdbcClient
        .sql(
            "INSERT INTO supermarkets (id, name, country, created_at, updated_at)"
                + " VALUES (:id, :name, 'ES', NOW(), NOW())")
        .param("id", id)
        .param("name", name)
        .update();
    insertedSupermarkets.add(id);
    return id;
  }

  private UUID insertCategory(UUID supermarketId) {
    var id = UUID.randomUUID();
    jdbcClient
        .sql(
            "INSERT INTO categories (id, external_id, supermarket_id, name, level_type,"
                + " created_at, updated_at)"
                + " VALUES (:id, :extId, :smId, 'Test Category', 'LEAF', NOW(), NOW())")
        .param("id", id)
        .param("extId", "CAT-" + id)
        .param("smId", supermarketId)
        .update();
    insertedCategories.add(id);
    return id;
  }

  private UUID insertProduct(UUID supermarketId, UUID catId, String name, String extId) {
    var id = UUID.randomUUID();
    jdbcClient
        .sql(
            "INSERT INTO products (id, external_id, supermarket_id, category_id, name,"
                + " is_active, created_at, updated_at)"
                + " VALUES (:id, :extId, :smId, :catId, :name, true, NOW(), NOW())")
        .param("id", id)
        .param("extId", extId)
        .param("smId", supermarketId)
        .param("catId", catId)
        .param("name", name)
        .update();
    insertedProducts.add(id);
    return id;
  }

  private void insertPrice(UUID productId, double unitPrice) {
    jdbcClient
        .sql(
            "INSERT INTO product_prices (id, product_id, unit_price, selling_method, currency,"
                + " recorded_at) VALUES (:id, :productId, :price, 0, 'EUR', NOW())")
        .param("id", UUID.randomUUID())
        .param("productId", productId)
        .param("price", unitPrice)
        .update();
  }
}
