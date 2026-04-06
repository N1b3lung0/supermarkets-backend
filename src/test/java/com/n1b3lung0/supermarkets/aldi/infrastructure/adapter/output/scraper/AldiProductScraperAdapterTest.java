package com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper;

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

/** Step 77 — verifies AldiProductScraperAdapter using MockRestServiceServer + fixtures. */
class AldiProductScraperAdapterTest {

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000004"));
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
                    .contains("/api/front/v1/products")
                    .contains("lacteos"))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/aldi/products_lacteos_page0.json"),
                MediaType.APPLICATION_JSON));

    var adapter = new AldiProductScraperAdapter(RestClient.create(restTemplate));
    var index = Map.of(LACTEOS_EXT, LACTEOS_ID);

    var commands = adapter.fetchProductsBySubcategory(SUPERMARKET, LACTEOS_EXT, index);

    assertThat(commands).hasSize(3);
    assertThat(commands.get(0).name()).contains("Leche");
    assertThat(commands.get(0).externalId()).isEqualTo("24356");
    assertThat(commands.get(0).ean()).isEqualTo("4056489956037");
    assertThat(commands.get(0).brand()).isEqualTo("ALDI");
    assertThat(commands.get(0).priceInstructions()).isNotNull();
    assertThat(commands.get(0).priceInstructions().unitPrice().amount())
        .isEqualByComparingTo("0.65");
  }

  @Test
  void fetchProducts_unknownCategoryId_returnsEmpty() {
    var restTemplate = new RestTemplate();
    // No HTTP call should be made since the category is not in the index
    var adapter = new AldiProductScraperAdapter(RestClient.create(restTemplate));

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

    var adapter = new AldiProductScraperAdapter(RestClient.create(restTemplate));

    assertThatThrownBy(
            () ->
                adapter.fetchProductsBySubcategory(
                    SUPERMARKET, LACTEOS_EXT, Map.of(LACTEOS_EXT, LACTEOS_ID)))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("ALDI");
  }

  @Test
  void supports_returnsTrueForAldiUuid() {
    var adapter = new AldiProductScraperAdapter(RestClient.create());
    assertThat(adapter.supports(SUPERMARKET)).isTrue();
    assertThat(
            adapter.supports(
                SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
        .isFalse();
  }
}
