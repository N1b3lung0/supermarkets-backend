package com.n1b3lung0.supermarkets.aldi.infrastructure.adapter.output.scraper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.n1b3lung0.supermarkets.shared.infrastructure.exception.ExternalServiceException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/** Step 77 — verifies AldiCategoryScraperAdapter using MockRestServiceServer + fixtures. */
class AldiCategoryScraperAdapterTest {

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000004"));

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
  void fetchCategories_returnsTopAndSubCommands() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request ->
                assertThat(request.getURI().toString())
                    .contains("/api/front/v1/categories")
                    .contains("lang=es"))
        .andRespond(
            withSuccess(loadFixture("/fixtures/aldi/categories.json"), MediaType.APPLICATION_JSON));

    var adapter = new AldiCategoryScraperAdapter(RestClient.create(restTemplate));
    var commands = adapter.fetchCategories(SUPERMARKET);

    assertThat(commands).isNotEmpty();
    assertThat(commands.stream().anyMatch(c -> "TOP".equals(c.levelType()))).isTrue();
    assertThat(commands.stream().anyMatch(c -> "SUB".equals(c.levelType()))).isTrue();
    commands.stream()
        .filter(c -> "TOP".equals(c.levelType()))
        .forEach(c -> assertThat(c.parentId()).isNull());
  }

  @Test
  void fetchCategories_fixture_hasExpectedTopCategories() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/front/v1/categories"))
        .andRespond(
            withSuccess(loadFixture("/fixtures/aldi/categories.json"), MediaType.APPLICATION_JSON));

    var adapter = new AldiCategoryScraperAdapter(RestClient.create(restTemplate));
    var commands = adapter.fetchCategories(SUPERMARKET);

    var topNames =
        commands.stream().filter(c -> "TOP".equals(c.levelType())).map(c -> c.name()).toList();
    assertThat(topNames).containsExactlyInAnyOrder("Frescos", "Bebidas");
  }

  @Test
  void fetchCategories_403Response_throwsExternalServiceException() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).build();
    server
        .expect(
            ExpectedCount.once(),
            request -> assertThat(request.getURI().toString()).contains("/api/front/v1/categories"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    var adapter = new AldiCategoryScraperAdapter(RestClient.create(restTemplate));

    assertThatThrownBy(() -> adapter.fetchCategories(SUPERMARKET))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("ALDI");
  }

  @Test
  void supports_returnsTrueForAldiUuid() {
    var adapter = new AldiCategoryScraperAdapter(RestClient.create());
    assertThat(adapter.supports(SUPERMARKET)).isTrue();
    assertThat(
            adapter.supports(
                SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
        .isFalse();
  }
}
