package com.n1b3lung0.supermarkets.sync.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

/**
 * Step 98 — E2E integration test for the full Mercadona catalog sync.
 *
 * <p>Boots the full Spring context against a real PostgreSQL database (Testcontainers). HTTP calls
 * to the Mercadona API are intercepted by {@link MockRestServiceServer} which returns local fixture
 * files. After triggering {@code POST /api/v1/sync/supermarkets/{mercadonaId}}, the test asserts:
 *
 * <ul>
 *   <li>The {@code SyncRun} is persisted with status {@code COMPLETED} and non-zero counters.
 *   <li>Categories (TOP, SUB, LEAF) are persisted in the {@code categories} table.
 *   <li>Products are persisted in the {@code products} table.
 *   <li>Product prices are recorded in the {@code product_prices} table.
 * </ul>
 */
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
class MercadonaSyncE2ETest extends PostgresIntegrationTest {

  private static final UUID MERCADONA_UUID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");

  /**
   * Minimal valid stub for any subcategory that has no fixture in the test classpath. The empty
   * {@code categories} array means the category scraper emits no LEAF commands and the product
   * scraper fetches no products for those subcategories.
   */
  private static final String EMPTY_SUBCATEGORY_STUB =
      "{\"id\":0,\"name\":\"stub\",\"order\":0,\"layout\":1,"
          + "\"published\":true,\"is_extended\":false,\"categories\":[]}";

  @Autowired private WebApplicationContext wac;
  @Autowired private RestTemplate mercadonaRestTemplate;
  @Autowired private JdbcTemplate jdbcTemplate;

  private MockMvc mockMvc;
  private MockRestServiceServer mockServer;

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
    mockServer =
        MockRestServiceServer.bindTo(mercadonaRestTemplate).ignoreExpectOrder(true).build();
    cleanUpMercadonaData();
  }

  // ---------------------------------------------------------------------------
  // E2E test
  // ---------------------------------------------------------------------------

  @Test
  void fullMercadonaSync_shouldCompleteAndPersistCategoriesProductsAndPrices() throws Exception {
    // given — stub the Mercadona catalogue API
    //   The category scraper calls GET /categories/ once to retrieve the 26 top categories.
    mockServer
        .expect(ExpectedCount.once(), requestTo(endsWith("/categories/")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/categories.json"), MediaType.APPLICATION_JSON));

    //   Subcategory 112 (Aceite, vinagre y sal) has a full fixture with leaf groups and products.
    //   It is called twice: once by the category scraper (leaf-group discovery) and once by the
    //   product scraper (product fetch). All other subcategory calls use the empty stub below.
    mockServer
        .expect(ExpectedCount.twice(), requestTo(endsWith("/categories/112")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/subcategory_112.json"),
                MediaType.APPLICATION_JSON));

    //   Catch-all for every other /categories/{id} request (151 × 2 = 302 calls).
    mockServer
        .expect(ExpectedCount.manyTimes(), requestTo(containsString("/categories/")))
        .andRespond(withSuccess(EMPTY_SUBCATEGORY_STUB, MediaType.APPLICATION_JSON));

    //   Product detail calls: GET /products/{id} is now called once per product to enrich
    //   fields like ean, brand, origin, etc. (34 products from subcategory 112).
    mockServer
        .expect(ExpectedCount.manyTimes(), requestTo(containsString("/products/")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/product_generic_detail.json"),
                MediaType.APPLICATION_JSON));

    // when — trigger the sync via the REST API
    mockMvc
        .perform(post("/api/v1/sync/supermarkets/{id}", MERCADONA_UUID).with(jwt()))
        .andExpect(status().isAccepted());

    // then — SyncRun is persisted as COMPLETED
    mockMvc
        .perform(
            get("/api/v1/sync/runs").param("supermarketId", MERCADONA_UUID.toString()).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$.content[0].categoriesSynced").value(greaterThan(0)))
        .andExpect(jsonPath("$.content[0].productsSynced").value(greaterThan(0)));

    // then — categories are persisted (TOP + SUB from the full fixture, LEAF from sub-112)
    var categoriesCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM categories WHERE supermarket_id = ?", Long.class, MERCADONA_UUID);
    assertThat(categoriesCount).isGreaterThan(0L);

    // then — products are persisted (all come from subcategory 112 leaf groups)
    var productsCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM products WHERE supermarket_id = ?", Long.class, MERCADONA_UUID);
    assertThat(productsCount).isGreaterThan(0L);

    // then — a price snapshot is recorded for every product
    var pricesCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM product_prices pp"
                + " JOIN products p ON p.id = pp.product_id"
                + " WHERE p.supermarket_id = ?",
            Long.class,
            MERCADONA_UUID);
    assertThat(pricesCount).isEqualTo(productsCount);

    // then — all mocked HTTP expectations were satisfied (no uncalled stubs)
    mockServer.verify();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Deletes all Mercadona rows before each test run to ensure idempotency. */
  private void cleanUpMercadonaData() {
    // product_prices → products → categories (FK order; leaf-first for self-referential categories)
    jdbcTemplate.update(
        "DELETE FROM product_prices"
            + " WHERE product_id IN (SELECT id FROM products WHERE supermarket_id = ?)",
        MERCADONA_UUID);
    jdbcTemplate.update(
        "DELETE FROM product_suppliers"
            + " WHERE product_id IN (SELECT id FROM products WHERE supermarket_id = ?)",
        MERCADONA_UUID);
    jdbcTemplate.update("DELETE FROM products WHERE supermarket_id = ?", MERCADONA_UUID);
    jdbcTemplate.update(
        "DELETE FROM categories WHERE supermarket_id = ? AND level_type = 'LEAF'", MERCADONA_UUID);
    jdbcTemplate.update(
        "DELETE FROM categories WHERE supermarket_id = ? AND level_type = 'SUB'", MERCADONA_UUID);
    jdbcTemplate.update(
        "DELETE FROM categories WHERE supermarket_id = ? AND level_type = 'TOP'", MERCADONA_UUID);
    jdbcTemplate.update("DELETE FROM sync_runs WHERE supermarket_id = ?", MERCADONA_UUID);
  }

  /** Loads a fixture file from the test classpath as a UTF-8 string. */
  private String loadFixture(String classpathPath) {
    try (var is = getClass().getResourceAsStream(classpathPath)) {
      if (is == null) {
        throw new IllegalStateException("Fixture not found on classpath: " + classpathPath);
      }
      return new String(is.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException("Failed to load fixture: " + classpathPath, e);
    }
  }

  // ---------------------------------------------------------------------------
  // Test configuration — replace the Mercadona RestClient with a testable one
  // that can be intercepted by MockRestServiceServer.
  //
  // WHY the lambda requestFactory:
  //   The Spring context builds beans at startup.  RestClient.create(restTemplate) copies
  //   the factory at that moment.  MockRestServiceServer.bindTo(restTemplate) replaces the
  //   factory *later* in setUp() — after the RestClient is already built.
  //   Using a lambda that calls restTemplate.getRequestFactory() at *request* time ensures
  //   the mock factory installed by bindTo() is always picked up.
  //
  // WHY the base URL:
  //   The scrapers call restClient.get().uri("/categories/") with a relative path.
  //   Without a base URL the factory receives a non-absolute URI and throws
  //   "URI is not absolute".  We use the same base URL as production so that
  //   the resulting absolute URL (https://tienda.mercadona.es/api/categories/)
  //   matches the MockRestServiceServer patterns (endsWith, containsString).
  //
  // The bean named "mercadonaRestClient" defined in MercadonaScraperConfig is
  // overridden here (requires spring.main.allow-bean-definition-overriding=true).
  // ---------------------------------------------------------------------------

  @TestConfiguration
  static class MercadonaMockConfig {

    /** Exposed as a bean so MockRestServiceServer can be bound to it in the test. */
    @Bean
    RestTemplate mercadonaRestTemplate() {
      return new RestTemplate();
    }

    /**
     * Overrides the production Mercadona RestClient.
     *
     * <p>The lambda requestFactory resolves {@code mercadonaRestTemplate.getRequestFactory()} at
     * every request, so the mock factory installed by {@link
     * MockRestServiceServer#bindTo(RestTemplate)} in {@link #setUp()} is always used.
     */
    @Bean("mercadonaRestClient")
    @Primary
    RestClient mercadonaRestClient(RestTemplate mercadonaRestTemplate) {
      return RestClient.builder()
          .requestFactory(
              (uri, method) -> mercadonaRestTemplate.getRequestFactory().createRequest(uri, method))
          .baseUrl("https://tienda.mercadona.es/api")
          .defaultHeader("User-Agent", "Mozilla/5.0")
          .defaultHeader("Accept", "application/json")
          .build();
    }
  }
}
