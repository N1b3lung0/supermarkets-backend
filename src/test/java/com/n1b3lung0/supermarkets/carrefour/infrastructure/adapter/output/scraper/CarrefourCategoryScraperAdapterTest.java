package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.mapper.CarrefourCategoryMapper;
import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/** Step 75 — verifies CarrefourCategoryScraperAdapter using MockRestServiceServer + fixtures. */
class CarrefourCategoryScraperAdapterTest {

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));

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

  private record AdapterAndServer(
      CarrefourCategoryScraperAdapter adapter, MockRestServiceServer server) {}

  private AdapterAndServer build(MockRestServiceServer server) {
    var restTemplate = new RestTemplate();
    var srv = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    var adapter =
        new CarrefourCategoryScraperAdapter(
            RestClient.create(restTemplate), new CarrefourCategoryMapper());
    return new AdapterAndServer(adapter, srv);
  }

  @Test
  void fetchCategories_shouldReturnTopAndSubCommands() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            org.springframework.test.web.client.ExpectedCount.once(),
            request -> {
              var uri = request.getURI().toString();
              assertThat(uri).contains("/api/2.0/page").contains("supermercado");
            })
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/carrefour/categories.json"), MediaType.APPLICATION_JSON));

    var adapter =
        new CarrefourCategoryScraperAdapter(
            RestClient.create(restTemplate), new CarrefourCategoryMapper());

    var commands = adapter.fetchCategories(SUPERMARKET);

    assertThat(commands).isNotEmpty();
    assertThat(commands.stream().anyMatch(c -> "TOP".equals(c.levelType()))).isTrue();
    assertThat(commands.stream().anyMatch(c -> "SUB".equals(c.levelType()))).isTrue();
    commands.stream()
        .filter(c -> "TOP".equals(c.levelType()))
        .forEach(c -> assertThat(c.parentExternalId()).isNull());
  }

  @Test
  void fetchCategories_fixture_hasExpectedTopCategories() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            org.springframework.test.web.client.ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/2.0/page"))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/carrefour/categories.json"), MediaType.APPLICATION_JSON));

    var adapter =
        new CarrefourCategoryScraperAdapter(
            RestClient.create(restTemplate), new CarrefourCategoryMapper());

    var commands = adapter.fetchCategories(SUPERMARKET);

    var topNames =
        commands.stream().filter(c -> "TOP".equals(c.levelType())).map(c -> c.name()).toList();
    assertThat(topNames).containsExactlyInAnyOrder("Alimentación", "Bebidas");
  }

  @Test
  void fetchCategories_403Response_throwsExternalServiceException() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            org.springframework.test.web.client.ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/2.0/page"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    var adapter =
        new CarrefourCategoryScraperAdapter(
            RestClient.create(restTemplate), new CarrefourCategoryMapper());

    assertThatThrownBy(() -> adapter.fetchCategories(SUPERMARKET))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("Carrefour");
  }
}
