package com.n1b3lung0.supermarkets.comparison.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.n1b3lung0.supermarkets.comparison.application.dto.CompareProductsByNameQuery;
import com.n1b3lung0.supermarkets.comparison.application.port.output.ProductComparisonQueryPort;
import com.n1b3lung0.supermarkets.comparison.domain.model.ProductMatch;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Step 64 — unit tests for CompareProductsByNameHandler. */
@ExtendWith(MockitoExtension.class)
class CompareProductsByNameHandlerTest {

  @Mock private ProductComparisonQueryPort queryPort;

  private CompareProductsByNameHandler handler() {
    return new CompareProductsByNameHandler(queryPort);
  }

  private ProductMatch match(String supermarket, double price) {
    return new ProductMatch(
        UUID.randomUUID(),
        UUID.randomUUID(),
        supermarket,
        "Leche 1L",
        Money.ofEur(BigDecimal.valueOf(price)),
        null,
        null,
        null,
        Instant.now());
  }

  @Test
  void execute_multipleMatches_returnsSortedByPriceWithCheapest() {
    var mercadona = match("Mercadona", 0.89);
    var lidl = match("LIDL", 0.69);
    var carrefour = match("Carrefour", 1.05);
    given(queryPort.findMatchesByName("leche", List.of()))
        .willReturn(List.of(mercadona, carrefour, lidl));

    var result = handler().execute(new CompareProductsByNameQuery("leche", List.of()));

    assertThat(result.matches()).hasSize(3);
    assertThat(result.matches().get(0).unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(0.69));
    assertThat(result.cheapestSupermarketName()).isEqualTo("LIDL");
  }

  @Test
  void execute_singleMatch_returnsCheapestCorrectly() {
    var only = match("Mercadona", 0.85);
    given(queryPort.findMatchesByName("leche", List.of())).willReturn(List.of(only));

    var result = handler().execute(new CompareProductsByNameQuery("leche", List.of()));

    assertThat(result.matches()).hasSize(1);
    assertThat(result.cheapestSupermarketId()).isEqualTo(only.supermarketId());
  }

  @Test
  void execute_noMatches_returnsEmptyViewWithNullCheapest() {
    given(queryPort.findMatchesByName("xyz", List.of())).willReturn(List.of());

    var result = handler().execute(new CompareProductsByNameQuery("xyz", List.of()));

    assertThat(result.matches()).isEmpty();
    assertThat(result.cheapestSupermarketId()).isNull();
    assertThat(result.cheapestSupermarketName()).isNull();
  }
}
