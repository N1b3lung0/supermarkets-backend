package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper;

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

/** Step 75 — verifies CarrefourProductScraperAdapter using MockRestServiceServer + fixtures. */
class CarrefourProductScraperAdapterTest {

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));
  private static final CategoryId LACTEOS_ID = CategoryId.generate();
  private static final ExternalCategoryId LACTEOS_EXT = ExternalCategoryId.of("lacteos");

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

  @Test
  void fetchProducts_singlePage_returnsAllProducts() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request ->
                assertThat(request.getURI().toString())
                    .contains("/api/2.0/products/search")
                    .contains("lacteos"))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/carrefour/products_lacteos_page0.json"),
                MediaType.APPLICATION_JSON));

    var adapter = new CarrefourProductScraperAdapter(RestClient.create(restTemplate));
    var index = Map.of(LACTEOS_EXT, LACTEOS_ID);

    var commands = adapter.fetchProductsBySubcategory(SUPERMARKET, LACTEOS_EXT, index);

    assertThat(commands).hasSize(3);
    assertThat(commands.get(0).name()).contains("Leche");
    assertThat(commands.get(0).externalId()).isEqualTo("8480017178466");
    assertThat(commands.get(0).priceInstructions()).isNotNull();
    assertThat(commands.get(0).priceInstructions().unitPrice().amount())
        .isEqualByComparingTo("0.89");
  }

  @Test
  void fetchProducts_unknownCategoryCode_skipsProduct() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("lacteos"))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/carrefour/products_lacteos_page0.json"),
                MediaType.APPLICATION_JSON));

    var adapter = new CarrefourProductScraperAdapter(RestClient.create(restTemplate));
    // Empty index — none of the products' categories will match
    var commands = adapter.fetchProductsBySubcategory(SUPERMARKET, LACTEOS_EXT, Map.of());

    assertThat(commands).isEmpty();
  }

  @Test
  void fetchProducts_403Response_throwsExternalServiceException() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("lacteos"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    var adapter = new CarrefourProductScraperAdapter(RestClient.create(restTemplate));

    assertThatThrownBy(
            () ->
                adapter.fetchProductsBySubcategory(
                    SUPERMARKET, LACTEOS_EXT, Map.of(LACTEOS_EXT, LACTEOS_ID)))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("Carrefour");
  }
}
