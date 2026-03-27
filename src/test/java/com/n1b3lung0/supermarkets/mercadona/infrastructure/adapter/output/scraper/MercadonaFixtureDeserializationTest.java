package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaCategoriesResponse;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaLevel1DetailDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaProductDetailDto;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * Step 47 — verifies that all three fixture JSON files are valid and can be deserialized by Jackson
 * into the correct DTO types.
 */
class MercadonaFixtureDeserializationTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void categories_fixture_shouldDeserializeCorrectly() throws IOException {
    try (InputStream is = getClass().getResourceAsStream("/fixtures/mercadona/categories.json")) {
      assertThat(is).isNotNull();
      var response = mapper.readValue(is, MercadonaCategoriesResponse.class);
      assertThat(response).isNotNull();
      assertThat(response.results()).isNotEmpty();
    }
  }

  @Test
  void subcategory112_fixture_shouldDeserializeCorrectly() throws IOException {
    try (InputStream is =
        getClass().getResourceAsStream("/fixtures/mercadona/subcategory_112.json")) {
      assertThat(is).isNotNull();
      var detail = mapper.readValue(is, MercadonaLevel1DetailDto.class);
      assertThat(detail).isNotNull();
      assertThat(detail.id()).isEqualTo(112);
      assertThat(detail.categories()).hasSize(4);
    }
  }

  @Test
  void product3400_fixture_shouldDeserializeCorrectly() throws IOException {
    try (InputStream is = getClass().getResourceAsStream("/fixtures/mercadona/product_3400.json")) {
      assertThat(is).isNotNull();
      var product = mapper.readValue(is, MercadonaProductDetailDto.class);
      assertThat(product).isNotNull();
      assertThat(product.id()).isEqualTo("3400");
      assertThat(product.ean()).isEqualTo("2105100034004");
    }
  }

  @Test
  void subcategory112_shouldHave34TotalProducts() throws IOException {
    try (InputStream is =
        getClass().getResourceAsStream("/fixtures/mercadona/subcategory_112.json")) {
      var detail = mapper.readValue(is, MercadonaLevel1DetailDto.class);
      int total = detail.categories().stream().mapToInt(g -> g.products().size()).sum();
      assertThat(total).isEqualTo(34);
    }
  }

  @Test
  void subcategory112_firstProduct_shouldHaveExpectedPriceFields() throws IOException {
    try (InputStream is =
        getClass().getResourceAsStream("/fixtures/mercadona/subcategory_112.json")) {
      var detail = mapper.readValue(is, MercadonaLevel1DetailDto.class);
      // group 420 "Aceite de oliva", first product "4241"
      var group420 =
          detail.categories().stream().filter(g -> g.id() == 420).findFirst().orElseThrow();
      var product4241 =
          group420.products().stream().filter(p -> p.id().equals("4241")).findFirst().orElseThrow();

      var pi = product4241.priceInstructions();
      assertThat(pi.unitPrice()).isEqualTo("19.75");
      assertThat(pi.bulkPrice()).isEqualTo("3.95");
      assertThat(pi.unitSize()).isEqualTo(5.0);
      assertThat(pi.iva()).isNull();
    }
  }

  @Test
  void subcategory112_product4641_shouldHaveLeadingWhitespacePreviousPrice() throws IOException {
    try (InputStream is =
        getClass().getResourceAsStream("/fixtures/mercadona/subcategory_112.json")) {
      var detail = mapper.readValue(is, MercadonaLevel1DetailDto.class);
      var group420 =
          detail.categories().stream().filter(g -> g.id() == 420).findFirst().orElseThrow();
      var product4641 =
          group420.products().stream().filter(p -> p.id().equals("4641")).findFirst().orElseThrow();

      // Raw value has leading spaces — the mapper strips them
      assertThat(product4641.priceInstructions().previousUnitPrice()).isNotNull();
      assertThat(product4641.priceInstructions().previousUnitPrice().strip()).isEqualTo("20.85");
    }
  }

  @Test
  void product3400_shouldHaveBulkAndVariableWeightAndEightSuppliers() throws IOException {
    try (InputStream is = getClass().getResourceAsStream("/fixtures/mercadona/product_3400.json")) {
      var product = mapper.readValue(is, MercadonaProductDetailDto.class);
      assertThat(product.isBulk()).isTrue();
      assertThat(product.isVariableWeight()).isTrue();
      assertThat(product.details().suppliers()).hasSize(8);
    }
  }
}
