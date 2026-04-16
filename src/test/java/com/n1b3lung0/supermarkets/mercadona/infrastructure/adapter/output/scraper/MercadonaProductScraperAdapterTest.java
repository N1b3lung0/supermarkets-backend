package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper.MercadonaPriceInstructionsMapper;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/** Step 52 — verifies MercadonaProductScraperAdapter using MockRestServiceServer. */
class MercadonaProductScraperAdapterTest {

  private MockRestServiceServer server;
  private MercadonaProductScraperAdapter adapter;

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));

  private static final CategoryId CAT_420 = CategoryId.generate();
  private static final CategoryId CAT_422 = CategoryId.generate();
  private static final CategoryId CAT_421 = CategoryId.generate();
  private static final CategoryId CAT_424 = CategoryId.generate();

  private static final Map<ExternalCategoryId, CategoryId> LEAF_INDEX =
      Map.of(
          ExternalCategoryId.of("420"), CAT_420,
          ExternalCategoryId.of("422"), CAT_422,
          ExternalCategoryId.of("421"), CAT_421,
          ExternalCategoryId.of("424"), CAT_424);

  @BeforeEach
  void setUp() {
    var restTemplate = new RestTemplate();
    server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    // RestClient.create(restTemplate) sends relative URIs to MockRestServiceServer
    adapter =
        new MercadonaProductScraperAdapter(
            RestClient.create(restTemplate), new MercadonaPriceInstructionsMapper());
  }

  private String loadFixture(String path) {
    try (var is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        throw new IllegalStateException("Fixture not found: " + path);
      }
      return new String(is.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Registers mock expectations for the subcategory + 34 product detail calls. */
  private void expectSubcategoryAndDetails() {
    server
        .expect(requestTo(Matchers.endsWith("/categories/112")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/subcategory_112.json"),
                MediaType.APPLICATION_JSON));
    server
        .expect(times(34), requestTo(Matchers.containsString("/products/")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/product_generic_detail.json"),
                MediaType.APPLICATION_JSON));
  }

  @Test
  void fetchProductsBySubcategory_shouldReturn34Products() {
    expectSubcategoryAndDetails();

    var commands =
        adapter.fetchProductsBySubcategory(SUPERMARKET, ExternalCategoryId.of("112"), LEAF_INDEX);

    assertThat(commands).hasSize(34);
  }

  @Test
  void fetchProductsBySubcategory_product4241_shouldHaveCorrectPriceAndCategory() {
    expectSubcategoryAndDetails();

    var commands =
        adapter.fetchProductsBySubcategory(SUPERMARKET, ExternalCategoryId.of("112"), LEAF_INDEX);

    var cmd4241 =
        commands.stream().filter(c -> "4241".equals(c.externalId())).findFirst().orElseThrow();

    assertThat(cmd4241.name()).isEqualTo("Aceite de oliva 0,4\u00ba Hacendado");
    assertThat(cmd4241.categoryId()).isEqualTo(CAT_420.value());
    assertThat(cmd4241.priceInstructions().unitPrice().amount())
        .isEqualByComparingTo(new BigDecimal("19.75"));
    assertThat(cmd4241.priceInstructions().bulkPrice().amount())
        .isEqualByComparingTo(new BigDecimal("3.95"));
    assertThat(cmd4241.priceInstructions().iva()).isNull();
  }

  @Test
  void fetchProductsBySubcategory_product4641_previousUnitPriceShouldBeStripped() {
    expectSubcategoryAndDetails();

    var commands =
        adapter.fetchProductsBySubcategory(SUPERMARKET, ExternalCategoryId.of("112"), LEAF_INDEX);

    var cmd4641 =
        commands.stream().filter(c -> "4641".equals(c.externalId())).findFirst().orElseThrow();

    var prevPrice = cmd4641.priceInstructions().previousUnitPrice();
    assertThat(prevPrice).isNotNull();
    assertThat(prevPrice.amount()).isEqualByComparingTo(new BigDecimal("20.85"));
  }

  @Test
  void fetchProductsBySubcategory_detailFieldsShouldBePopulated() {
    expectSubcategoryAndDetails();

    var commands =
        adapter.fetchProductsBySubcategory(SUPERMARKET, ExternalCategoryId.of("112"), LEAF_INDEX);

    var any = commands.getFirst();
    // detail fields come from product_generic_detail.json fixture
    assertThat(any.ean()).isEqualTo("8480000004241");
    assertThat(any.brand()).isEqualTo("Hacendado");
    assertThat(any.origin()).isEqualTo("España");
    assertThat(any.legalName()).isEqualTo("Aceite de oliva 0,4º");
    assertThat(any.isBulk()).isFalse();
    assertThat(any.isVariableWeight()).isFalse();
    assertThat(any.supplierNames()).containsExactly("Proveedor Test S.A.");
    assertThat(any.ingredients()).isEqualTo("Aceite de oliva");
  }

  @Test
  void fetchProductsBySubcategory_emptyIndexShouldSkipAllProducts() {
    server
        .expect(requestTo(Matchers.endsWith("/categories/112")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/subcategory_112.json"),
                MediaType.APPLICATION_JSON));

    var commands =
        adapter.fetchProductsBySubcategory(SUPERMARKET, ExternalCategoryId.of("112"), Map.of());

    assertThat(commands).isEmpty();
  }
}
