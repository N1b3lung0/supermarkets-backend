package com.n1b3lung0.supermarkets.lidl.infrastructure.adapter.output.scraper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.category.domain.model.ExternalCategoryId;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/** Step 78 — verifies LidlCategoryScraperAdapter + LidlProductScraperAdapter. */
class LidlScraperAdapterTest {

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000005"));
  private static final CategoryId FRESCOS_ID = CategoryId.generate();
  private static final ExternalCategoryId FRESCOS_EXT = ExternalCategoryId.of("frescos");

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

  // ---- Category tests ----

  @Test
  void fetchCategories_returnsTopCommands() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/categories/lidl"))
        .andRespond(
            withSuccess(loadFixture("/fixtures/lidl/categories.json"), MediaType.APPLICATION_JSON));

    var adapter = new LidlCategoryScraperAdapter(RestClient.create(restTemplate));
    var commands = adapter.fetchCategories(SUPERMARKET);

    assertThat(commands).hasSize(3);
    assertThat(commands).allMatch(c -> "TOP".equals(c.levelType()));
    assertThat(commands).allMatch(c -> c.parentExternalId() == null);
    assertThat(commands.stream().map(c -> c.name()).toList())
        .containsExactlyInAnyOrder("Frescos y congelados", "Charcutería y quesos", "Bebidas");
  }

  @Test
  void fetchCategories_403_throwsExternalServiceException() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/categories/lidl"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    var adapter = new LidlCategoryScraperAdapter(RestClient.create(restTemplate));
    assertThatThrownBy(() -> adapter.fetchCategories(SUPERMARKET))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("LIDL");
  }

  @Test
  void categoryScraper_supports_returnsTrueForLidlUuid() {
    var adapter = new LidlCategoryScraperAdapter(RestClient.create());
    assertThat(adapter.supports(SUPERMARKET)).isTrue();
    assertThat(
            adapter.supports(
                SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
        .isFalse();
  }

  // ---- Product tests ----

  @Test
  void fetchProducts_returnsAllProducts() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request ->
                assertThat(request.getURI().toString())
                    .contains("/api/gridboxes/ES/es")
                    .contains("frescos"))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/lidl/products_frescos_page0.json"),
                MediaType.APPLICATION_JSON));

    var adapter = new LidlProductScraperAdapter(RestClient.create(restTemplate));
    var commands =
        adapter.fetchProductsBySubcategory(
            SUPERMARKET, FRESCOS_EXT, Map.of(FRESCOS_EXT, FRESCOS_ID));

    assertThat(commands).hasSize(2);
    assertThat(commands.get(0).name()).contains("Leche entera");
    assertThat(commands.get(0).externalId()).isEqualTo("4056489123456");
    assertThat(commands.get(0).priceInstructions().unitPrice().amount())
        .isEqualByComparingTo("0.72");
  }

  @Test
  void fetchProducts_unknownCategory_returnsEmpty() {
    var adapter = new LidlProductScraperAdapter(RestClient.create());
    var commands = adapter.fetchProductsBySubcategory(SUPERMARKET, FRESCOS_EXT, Map.of());
    assertThat(commands).isEmpty();
  }

  @Test
  void fetchProducts_403_throwsExternalServiceException() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("frescos"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    var adapter = new LidlProductScraperAdapter(RestClient.create(restTemplate));
    assertThatThrownBy(
            () ->
                adapter.fetchProductsBySubcategory(
                    SUPERMARKET, FRESCOS_EXT, Map.of(FRESCOS_EXT, FRESCOS_ID)))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("LIDL");
  }
}
