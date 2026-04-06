package com.n1b3lung0.supermarkets.performance;

import static org.assertj.core.api.Assertions.assertThat;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import com.n1b3lung0.supermarkets.comparison.application.dto.CompareProductsByNameQuery;
import com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.product.application.dto.GetProductByIdQuery;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductByIdUseCase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

/**
 * Step 86 — performance smoke test.
 *
 * <p>Seeds 10,000 products (with latest prices) and asserts:
 *
 * <ul>
 *   <li>Comparison query (cache miss, DB scan) finishes in &lt; 200 ms.
 *   <li>Product detail query (cache hit, second call) finishes in &lt; 50 ms.
 * </ul>
 *
 * <p>Uses {@code spring.cache.type=simple} (test profile) so the in-memory {@code
 * ConcurrentMapCacheManager} handles caching — no Redis required in CI.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PerformanceSmokeTest extends PostgresIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(PerformanceSmokeTest.class);

  private static final int PRODUCT_COUNT = 10_000;

  /** Six fixed supermarket UUIDs matching the seed data. */
  private static final UUID[] SUPERMARKET_IDS = {
    UUID.fromString("00000000-0000-0000-0000-000000000001"),
    UUID.fromString("00000000-0000-0000-0000-000000000002"),
    UUID.fromString("00000000-0000-0000-0000-000000000003"),
    UUID.fromString("00000000-0000-0000-0000-000000000004"),
    UUID.fromString("00000000-0000-0000-0000-000000000005"),
    UUID.fromString("00000000-0000-0000-0000-000000000006"),
  };

  @Autowired private JdbcClient jdbcClient;
  @Autowired private CompareProductsByNameUseCase compareProductsByNameUseCase;
  @Autowired private GetProductByIdUseCase getProductByIdUseCase;

  /** Product ID used for the cache-hit latency test. */
  private UUID sampleProductId;

  @BeforeAll
  void seedData() {
    log.info("[Perf] Seeding {} products…", PRODUCT_COUNT);

    // Shared category per supermarket — use a fixed UUID to avoid duplicates across test runs
    UUID categoryId = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    jdbcClient
        .sql(
            "INSERT INTO categories (id, external_id, supermarket_id, name, level_type,"
                + " created_at, updated_at)"
                + " VALUES (:id, 'PERF-CAT', :smId, 'Perf Category', 'LEAF', NOW(), NOW())"
                + " ON CONFLICT DO NOTHING")
        .param("id", categoryId)
        .param("smId", SUPERMARKET_IDS[0])
        .update();

    // Seed products — distribute evenly across 6 supermarkets
    String[] nameTemplates = {
      "leche entera",
      "leche semidesnatada",
      "aceite oliva",
      "aceite girasol",
      "pan integral",
      "pan blanco",
      "arroz largo",
      "pasta macarrones",
      "tomate triturado",
      "yogur natural"
    };

    sampleProductId = null;
    var productBatch = new StringBuilder();
    var priceBatch = new StringBuilder();
    int batchSize = 500;
    int inserted = 0;

    for (int i = 0; i < PRODUCT_COUNT; i++) {
      UUID productId = UUID.randomUUID();
      UUID smId = SUPERMARKET_IDS[i % SUPERMARKET_IDS.length];
      String name = nameTemplates[i % nameTemplates.length] + " " + i;
      String extId = "PERF-" + i;

      if (sampleProductId == null) {
        sampleProductId = productId;
      }

      jdbcClient
          .sql(
              "INSERT INTO products (id, external_id, supermarket_id, category_id, name,"
                  + " is_active, created_at, updated_at)"
                  + " VALUES (:id, :extId, :smId, :catId, :name, true, NOW(), NOW())"
                  + " ON CONFLICT DO NOTHING")
          .param("id", productId)
          .param("extId", extId)
          .param("smId", smId)
          .param("catId", categoryId)
          .param("name", name)
          .update();

      jdbcClient
          .sql(
              "INSERT INTO product_prices (id, product_id, unit_price, selling_method, currency,"
                  + " recorded_at) VALUES (:id, :productId, :price, 0, 'EUR', NOW())"
                  + " ON CONFLICT DO NOTHING")
          .param("id", UUID.randomUUID())
          .param("productId", productId)
          .param("price", 0.99 + (i % 100) * 0.10)
          .update();

      inserted++;
      if (inserted % batchSize == 0) {
        log.info("[Perf] Inserted {}/{} products", inserted, PRODUCT_COUNT);
      }
    }

    log.info("[Perf] Refreshing materialized view…");
    jdbcClient.sql("REFRESH MATERIALIZED VIEW latest_product_prices").update();
    log.info("[Perf] Seed complete. sampleProductId={}", sampleProductId);
  }

  @AfterAll
  void cleanUp() {
    log.info("[Perf] Cleaning up seeded data…");
    jdbcClient
        .sql(
            "DELETE FROM product_prices WHERE product_id IN"
                + " (SELECT id FROM products WHERE external_id LIKE 'PERF-%')")
        .update();
    jdbcClient.sql("DELETE FROM products WHERE external_id LIKE 'PERF-%'").update();
    jdbcClient
        .sql("DELETE FROM categories WHERE id = 'aaaaaaaa-0000-0000-0000-000000000001'::uuid")
        .update();
    jdbcClient.sql("REFRESH MATERIALIZED VIEW latest_product_prices").update();
  }

  @Test
  void compareQuery_withTenThousandProducts_respondsInUnder200ms() {
    // Warm up JIT — run once before measuring
    compareProductsByNameUseCase.execute(new CompareProductsByNameQuery("leche", List.of()));

    var sw = new StopWatch("compareQuery");
    sw.start();
    var result =
        compareProductsByNameUseCase.execute(new CompareProductsByNameQuery("leche", List.of()));
    sw.stop();

    long elapsedMs = sw.getTotalTimeMillis();
    log.info(
        "[Perf] compareQuery 'leche' → {} matches in {} ms", result.matches().size(), elapsedMs);

    assertThat(result.matches()).isNotEmpty();
    assertThat(elapsedMs)
        .as("Comparison query must complete in < 200 ms (was %d ms)", elapsedMs)
        .isLessThan(200);
  }

  @Test
  void getProductById_secondCall_respondsInUnder50ms() {
    assertThat(sampleProductId).isNotNull();

    // First call — cache miss, populates the in-memory cache
    getProductByIdUseCase.execute(new GetProductByIdQuery(sampleProductId));

    // Second call — should be a cache hit
    var sw = new StopWatch("getProductById-cacheHit");
    sw.start();
    var product = getProductByIdUseCase.execute(new GetProductByIdQuery(sampleProductId));
    sw.stop();

    long elapsedMs = sw.getTotalTimeMillis();
    log.info("[Perf] getProductById cache-hit for {} → {} ms", sampleProductId, elapsedMs);

    assertThat(product).isNotNull();
    assertThat(elapsedMs)
        .as("Product detail cache-hit must complete in < 50 ms (was %d ms)", elapsedMs)
        .isLessThan(50);
  }
}
