package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper.MercadonaCategoryMapper;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/** Step 51 — verifies MercadonaCategoryScraperAdapter using MockRestServiceServer. */
class MercadonaCategoryScraperAdapterTest {

  private static final SupermarketId SUPERMARKET =
      SupermarketId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
  private static final String BASE = "https://tienda.mercadona.es/api";
  private static final String STUB_EMPTY =
      "{\"id\":999,\"name\":\"stub\",\"order\":0,\"layout\":1,"
          + "\"published\":true,\"is_extended\":false,\"categories\":[]}";

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

  /** Builds a server+adapter pair where every /categories/{id} call returns an empty stub. */
  private record AdapterAndServer(
      MercadonaCategoryScraperAdapter adapter, MockRestServiceServer server) {}

  private AdapterAndServer buildWithEmptySubcategories() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    server
        .expect(ExpectedCount.once(), requestTo(org.hamcrest.Matchers.endsWith("/categories/")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/categories.json"), MediaType.APPLICATION_JSON));
    server
        .expect(
            ExpectedCount.manyTimes(),
            requestTo(org.hamcrest.Matchers.containsString("/categories/")))
        .andRespond(withSuccess(STUB_EMPTY, MediaType.APPLICATION_JSON));
    return new AdapterAndServer(
        new MercadonaCategoryScraperAdapter(
            RestClient.create(restTemplate), new MercadonaCategoryMapper()),
        server);
  }

  @Test
  void fetchCategories_shouldContainTopAndSubCommands() {
    var pair = buildWithEmptySubcategories();
    var commands = pair.adapter().fetchCategories(SUPERMARKET);

    assertThat(commands).isNotEmpty();
    assertThat(commands.stream().anyMatch(c -> "TOP".equals(c.levelType()))).isTrue();
    assertThat(commands.stream().anyMatch(c -> "SUB".equals(c.levelType()))).isTrue();
    commands.stream()
        .filter(c -> "TOP".equals(c.levelType()))
        .forEach(c -> assertThat(c.parentId()).isNull());
  }

  @Test
  void fetchCategories_top12_shouldHaveCorrectName() {
    var pair = buildWithEmptySubcategories();
    var commands = pair.adapter().fetchCategories(SUPERMARKET);

    var top12 =
        commands.stream()
            .filter(c -> "TOP".equals(c.levelType()) && "12".equals(c.externalId()))
            .findFirst();
    assertThat(top12).isPresent();
    assertThat(top12.get().name()).isEqualTo("Aceite, especias y salsas");
    assertThat(top12.get().parentId()).isNull();
  }

  @Test
  void fetchCategories_sub112_shouldHaveCorrectName() {
    var pair = buildWithEmptySubcategories();
    var commands = pair.adapter().fetchCategories(SUPERMARKET);

    var sub112 =
        commands.stream()
            .filter(c -> "SUB".equals(c.levelType()) && "112".equals(c.externalId()))
            .findFirst();
    assertThat(sub112).isPresent();
    assertThat(sub112.get().name()).isEqualTo("Aceite, vinagre y sal");
  }

  @Test
  void fetchCategories_withRealSubcategory112_shouldIncludeLeafGroups() {
    var restTemplate = new RestTemplate();
    var server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    server
        .expect(ExpectedCount.once(), requestTo(org.hamcrest.Matchers.endsWith("/categories/")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/categories.json"), MediaType.APPLICATION_JSON));
    server
        .expect(ExpectedCount.once(), requestTo(org.hamcrest.Matchers.endsWith("/categories/112")))
        .andRespond(
            withSuccess(
                loadFixture("/fixtures/mercadona/subcategory_112.json"),
                MediaType.APPLICATION_JSON));
    server
        .expect(
            ExpectedCount.manyTimes(),
            requestTo(org.hamcrest.Matchers.containsString("/categories/")))
        .andRespond(withSuccess(STUB_EMPTY, MediaType.APPLICATION_JSON));

    var adapter =
        new MercadonaCategoryScraperAdapter(
            RestClient.create(restTemplate), new MercadonaCategoryMapper());
    var commands = adapter.fetchCategories(SUPERMARKET);

    var leafIds =
        commands.stream()
            .filter(c -> "LEAF".equals(c.levelType()))
            .map(c -> c.externalId())
            .toList();
    assertThat(leafIds).contains("420", "422", "421", "424");
  }
}
