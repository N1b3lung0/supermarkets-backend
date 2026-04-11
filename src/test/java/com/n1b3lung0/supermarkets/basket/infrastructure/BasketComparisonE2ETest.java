package com.n1b3lung0.supermarkets.basket.infrastructure;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n1b3lung0.supermarkets.PostgresIntegrationTest;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Step 99 — E2E integration test for the basket comparison flow.
 *
 * <p>Boots the full Spring context against a real PostgreSQL database (Testcontainers). Inserts
 * products for two supermarkets (Mercadona and Carrefour) with known prices directly into the
 * database, then:
 *
 * <ol>
 *   <li>Creates a basket via {@code POST /api/v1/baskets}.
 *   <li>Adds 3 items via {@code POST /api/v1/baskets/{id}/items}.
 *   <li>Calls {@code GET /api/v1/baskets/{id}/compare}.
 *   <li>Asserts Mercadona is identified as the cheapest supermarket.
 * </ol>
 *
 * <p>Price setup (test-seeded data only — demo data from V15 may add additional supermarkets):
 *
 * <pre>
 *   Item           Qty  Mercadona  Carrefour
 *   -----          ---  ---------  ---------
 *   Leche            2     €0.89      €1.50
 *   Aceite girasol   1     €1.20      €1.80
 *   Yogur            3     €0.85      €1.20
 * </pre>
 *
 * Mercadona is always the cheapest supermarket regardless of demo data (which may add more
 * supermarkets at higher total costs).
 */
@SpringBootTest
@ActiveProfiles("test")
class BasketComparisonE2ETest extends PostgresIntegrationTest {

  // ── Supermarkets — seeded by V3 migration ──────────────────────────────────
  private static final UUID MERCADONA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  private static final UUID CARREFOUR_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  // ── Test categories — prefix 0xEE to avoid clashing with scraper data ──────
  private static final UUID CAT_MERCADONA = UUID.fromString("ee000000-0000-0000-0000-000000000001");

  private static final UUID CAT_CARREFOUR = UUID.fromString("ee000000-0000-0000-0000-000000000002");

  // ── Test products — Mercadona ───────────────────────────────────────────────
  private static final UUID PROD_MERCA_LECHE =
      UUID.fromString("ee000000-0000-0001-0000-000000000001");

  private static final UUID PROD_MERCA_ACEITE =
      UUID.fromString("ee000000-0000-0001-0000-000000000002");

  private static final UUID PROD_MERCA_YOGUR =
      UUID.fromString("ee000000-0000-0001-0000-000000000003");

  // ── Test products — Carrefour ───────────────────────────────────────────────
  private static final UUID PROD_CARRE_LECHE =
      UUID.fromString("ee000000-0000-0002-0000-000000000001");

  private static final UUID PROD_CARRE_ACEITE =
      UUID.fromString("ee000000-0000-0002-0000-000000000002");

  private static final UUID PROD_CARRE_YOGUR =
      UUID.fromString("ee000000-0000-0002-0000-000000000003");

  @Autowired private WebApplicationContext wac;
  @Autowired private JdbcTemplate jdbcTemplate;

  private MockMvc mockMvc;

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
    cleanUpTestData();
    insertTestData();
  }

  @AfterEach
  void tearDown() {
    cleanUpTestData();
  }

  // ---------------------------------------------------------------------------
  // E2E test
  // ---------------------------------------------------------------------------

  @Test
  void compareBasket_shouldIdentifyMercadonaAsCheapestSupermarket() throws Exception {
    // given — create a basket
    var createResult =
        mockMvc
            .perform(
                post("/api/v1/baskets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Mi cesta E2E\"}")
                    .with(jwt()))
            .andExpect(status().isCreated())
            .andReturn();

    var location = createResult.getResponse().getHeader("Location");
    var basketId = location.substring(location.lastIndexOf('/') + 1);

    // given — add 3 items (product names are search terms matched via ILIKE)
    // "Leche" × 2 → Mercadona 0.89×2 = 1.78 | Carrefour 1.50×2 = 3.00
    addItem(basketId, "Leche", 2);
    // "Aceite girasol" × 1 → Mercadona 1.20×1 = 1.20 | Carrefour 1.80×1 = 1.80
    addItem(basketId, "Aceite girasol", 1);
    // "Yogur" × 3 → Mercadona 0.85×3 = 2.55 | Carrefour 1.20×3 = 3.60
    addItem(basketId, "Yogur", 3);

    // when / then — compare
    mockMvc
        .perform(get("/api/v1/baskets/{id}/compare", basketId).with(jwt()))
        .andExpect(status().isOk())
        // cheapest supermarket is correctly identified
        .andExpect(jsonPath("$.cheapestSupermarketName").value("Mercadona"))
        // at least the two seeded test supermarkets (V15 demo data may add more)
        .andExpect(jsonPath("$.perSupermarket", hasSize(greaterThanOrEqualTo(2))))
        // Mercadona is always cheapest — always at index 0 (sorted ASC)
        .andExpect(jsonPath("$.perSupermarket[0].supermarketName").value("Mercadona"))
        // Mercadona has matches for all 3 basket items
        .andExpect(jsonPath("$.perSupermarket[0].itemMatches", hasSize(3)));
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private void addItem(String basketId, String productName, int quantity) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/baskets/{id}/items", basketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    String.format(
                        "{\"productName\":\"%s\",\"quantity\":%d}", productName, quantity))
                .with(jwt()))
        .andExpect(status().isCreated());
  }

  /**
   * Removes all test-specific rows in FK order. Uses external_id prefix {@code EE} to isolate test
   * products from any real scraper data.
   */
  private void cleanUpTestData() {
    jdbcTemplate.update("DELETE FROM baskets"); // CASCADE → basket_items
    jdbcTemplate.update(
        "DELETE FROM product_prices"
            + " WHERE product_id IN (SELECT id FROM products WHERE external_id LIKE 'EE%')");
    jdbcTemplate.update("DELETE FROM products WHERE external_id LIKE 'EE%'");
    jdbcTemplate.update(
        "DELETE FROM categories WHERE id = ? OR id = ?", CAT_MERCADONA, CAT_CARREFOUR);
  }

  /**
   * Inserts categories, products, prices and refreshes the {@code latest_product_prices}
   * materialized view so comparison queries find the seeded prices.
   */
  private void insertTestData() {
    // ── categories ────────────────────────────────────────────────────────────
    insertCategory(CAT_MERCADONA, "E001", MERCADONA_ID);
    insertCategory(CAT_CARREFOUR, "E001", CARREFOUR_ID);

    // ── products — Mercadona ──────────────────────────────────────────────────
    insertProduct(PROD_MERCA_LECHE, "EE001", MERCADONA_ID, CAT_MERCADONA, "Leche entera 1L");
    insertProduct(PROD_MERCA_ACEITE, "EE002", MERCADONA_ID, CAT_MERCADONA, "Aceite girasol 1L");
    insertProduct(PROD_MERCA_YOGUR, "EE003", MERCADONA_ID, CAT_MERCADONA, "Yogur natural");

    // ── products — Carrefour ──────────────────────────────────────────────────
    insertProduct(PROD_CARRE_LECHE, "EE101", CARREFOUR_ID, CAT_CARREFOUR, "Leche fresca 1L");
    insertProduct(
        PROD_CARRE_ACEITE, "EE102", CARREFOUR_ID, CAT_CARREFOUR, "Aceite girasol Carrefour");
    insertProduct(PROD_CARRE_YOGUR, "EE103", CARREFOUR_ID, CAT_CARREFOUR, "Yogur griego");

    // ── prices ────────────────────────────────────────────────────────────────
    insertPrice(PROD_MERCA_LECHE, "0.89");
    insertPrice(PROD_MERCA_ACEITE, "1.20");
    insertPrice(PROD_MERCA_YOGUR, "0.85");
    insertPrice(PROD_CARRE_LECHE, "1.50");
    insertPrice(PROD_CARRE_ACEITE, "1.80");
    insertPrice(PROD_CARRE_YOGUR, "1.20");

    // ── refresh comparison view ───────────────────────────────────────────────
    // Must run outside a transaction; Hikari uses autoCommit=true here since
    // @BeforeEach is not wrapped in any @Transactional scope.
    jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY latest_product_prices");
  }

  private void insertCategory(UUID id, String externalId, UUID supermarketId) {
    jdbcTemplate.update(
        "INSERT INTO categories (id, name, external_id, supermarket_id, level_type, sort_order)"
            + " VALUES (?, 'Alimentacion', ?, ?, 'TOP', 1)",
        id,
        externalId,
        supermarketId);
  }

  private void insertProduct(
      UUID id, String externalId, UUID supermarketId, UUID categoryId, String name) {
    jdbcTemplate.update(
        "INSERT INTO products"
            + " (id, external_id, supermarket_id, category_id, name, is_active, purchase_limit)"
            + " VALUES (?, ?, ?, ?, ?, true, 999)",
        id,
        externalId,
        supermarketId,
        categoryId,
        name);
  }

  private void insertPrice(UUID productId, String unitPrice) {
    jdbcTemplate.update(
        "INSERT INTO product_prices"
            + " (id, product_id, unit_price, selling_method, currency, recorded_at)"
            + " VALUES (gen_random_uuid(), ?, ?, 0, 'EUR', now())",
        productId,
        new BigDecimal(unitPrice));
  }
}
