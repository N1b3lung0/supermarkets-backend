package com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.output.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Step 65 — integration test for ProductComparisonJdbcAdapter using Testcontainers PostgreSQL.
 * Seeds supermarket + category + product + price data directly via SQL for full control.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductComparisonJdbcAdapterTest extends PostgresIntegrationTest {

  @Autowired private JdbcClient jdbcClient;

  private ProductComparisonJdbcAdapter adapter;

  private UUID mercadonaId;
  private UUID lidlId;
  private UUID categoryId;

  @BeforeEach
  void setUp() {
    adapter = new ProductComparisonJdbcAdapter(jdbcClient);

    mercadonaId = insertSupermarket("Mercadona Test");
    lidlId = insertSupermarket("LIDL Test");
    categoryId = insertCategory(mercadonaId);

    // Seed products + prices
    var aceite1 = insertProduct(mercadonaId, categoryId, "Aceite oliva virgen extra 1L", "EXT-1");
    var aceite2 = insertProduct(lidlId, categoryId, "Aceite oliva virgen extra 750ml", "EXT-2");
    var leche = insertProduct(mercadonaId, categoryId, "Leche entera 1L", "EXT-3");

    insertPrice(aceite1, 3.50);
    insertPrice(aceite2, 2.99);
    insertPrice(leche, 0.85);
  }

  @Test
  void findMatchesByName_searchAceite_returnsProductsFromAllSupermarkets() {
    var results = adapter.findMatchesByName("aceite", List.of());

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
    var lower = adapter.findMatchesByName("aceite", List.of());
    var upper = adapter.findMatchesByName("ACEITE", List.of());
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
    return id;
  }

  private UUID insertProduct(UUID supermarketId, UUID categoryId, String name, String extId) {
    var id = UUID.randomUUID();
    jdbcClient
        .sql(
            "INSERT INTO products (id, external_id, supermarket_id, category_id, name,"
                + " is_active, created_at, updated_at)"
                + " VALUES (:id, :extId, :smId, :catId, :name, true, NOW(), NOW())")
        .param("id", id)
        .param("extId", extId)
        .param("smId", supermarketId)
        .param("catId", categoryId)
        .param("name", name)
        .update();
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
