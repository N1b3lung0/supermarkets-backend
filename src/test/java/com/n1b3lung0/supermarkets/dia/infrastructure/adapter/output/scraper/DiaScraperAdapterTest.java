package com.n1b3lung0.supermarkets.dia.infrastructure.adapter.output.scraper;

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

/** Step 80 — verifies DiaCategoryScraperAdapter + DiaProductScraperAdapter. */
class DiaScraperAdapterTest {

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000006"));
  private static final CategoryId LACTEOS_ID = CategoryId.generate();
  private static final ExternalCategoryId LACTEOS_EXT = ExternalCategoryId.of("lacteos-frescos");

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
  void fetchCategories_returnsTopAndSubCommands() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/catalog/categories"))
        .andRespond(
            withSuccess(loadFixture("/fixtures/dia/categories.json"), MediaType.APPLICATION_JSON));

    var adapter = new DiaCategoryScraperAdapter(RestClient.create(restTemplate));
    var commands = adapter.fetchCategories(SUPERMARKET);

    assertThat(commands).isNotEmpty();
    assertThat(commands.stream().anyMatch(c -> "TOP".equals(c.levelType()))).isTrue();
    assertThat(commands.stream().anyMatch(c -> "SUB".equals(c.levelType()))).isTrue();
    var topNames =
        commands.stream().filter(c -> "TOP".equals(c.levelType())).map(c -> c.name()).toList();
    assertThat(topNames).containsExactlyInAnyOrder("Frescos", "Bebidas");
  }

  @Test
  void fetchCategories_403_throwsExternalServiceException() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/catalog/categories"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    var adapter = new DiaCategoryScraperAdapter(RestClient.create(restTemplate));
    assertThatThrownBy(() -> adapter.fetchCategories(SUPERMARKET))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("DIA");
  }

  @Test
  void categoryScraper_supports_returnsTrueForDiaUuid() {
    var adapter = new DiaCategoryScraperAdapter(RestClient.create());
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
                    .contains("/api/catalog/products")
                    .contains("lacteos-frescos"))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/dia/products_lacteos_page0.json"),
                MediaType.APPLICATION_JSON));

    var adapter = new DiaProductScraperAdapter(RestClient.create(restTemplate));
    var commands =
        adapter.fetchProductsBySubcategory(
            SUPERMARKET, LACTEOS_EXT, Map.of(LACTEOS_EXT, LACTEOS_ID));

    assertThat(commands).hasSize(2);
    assertThat(commands.get(0).name()).contains("Leche entera");
    assertThat(commands.get(0).externalId()).isEqualTo("8480017028469");
    assertThat(commands.get(0).priceInstructions().unitPrice().amount())
        .isEqualByComparingTo("0.63");
  }

  @Test
  void fetchProducts_unknownCategory_returnsEmpty() {
    var adapter = new DiaProductScraperAdapter(RestClient.create());
    assertThat(adapter.fetchProductsBySubcategory(SUPERMARKET, LACTEOS_EXT, Map.of())).isEmpty();
  }

  @Test
  void fetchProducts_403_throwsExternalServiceException() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/catalog/products"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    var adapter = new DiaProductScraperAdapter(RestClient.create(restTemplate));
    assertThatThrownBy(
            () ->
                adapter.fetchProductsBySubcategory(
                    SUPERMARKET, LACTEOS_EXT, Map.of(LACTEOS_EXT, LACTEOS_ID)))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("DIA");
  }
}
