package com.n1b3lung0.supermarkets.comparison.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.n1b3lung0.supermarkets.comparison.domain.model.ProductComparison;
import com.n1b3lung0.supermarkets.comparison.domain.model.ProductMatch;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Step 62 — unit tests for ProductComparison domain model. */
class ProductComparisonTest {

  private static ProductMatch match(String supermarketName, String productName, double price) {
    return new ProductMatch(
        UUID.randomUUID(),
        UUID.randomUUID(),
        supermarketName,
        productName,
        Money.ofEur(BigDecimal.valueOf(price)),
        null,
        null,
        null,
        Instant.now());
  }

  @Test
  void cheapest_returnsMatchWithLowestPrice() {
    var mercadona = match("Mercadona", "Aceite oliva 1L", 3.50);
    var carrefour = match("Carrefour", "Aceite oliva 1L", 4.20);
    var lidl = match("LIDL", "Aceite oliva 1L", 2.99);

    var comparison = ProductComparison.of("aceite", List.of(mercadona, carrefour, lidl));

    assertThat(comparison.cheapest()).isPresent();
    assertThat(comparison.cheapest().get()).isEqualTo(lidl);
  }

  @Test
  void cheapest_whenEmpty_returnsEmptyOptional() {
    var comparison = ProductComparison.of("aceite", List.of());
    assertThat(comparison.cheapest()).isEmpty();
  }

  @Test
  void cheapest_whenSingleMatch_returnsIt() {
    var only = match("Mercadona", "Leche 1L", 0.85);
    var comparison = ProductComparison.of("leche", List.of(only));
    assertThat(comparison.cheapest()).contains(only);
  }

  @Test
  void sortedByPrice_returnMatchesAscending() {
    var cheap = match("LIDL", "Leche 1L", 0.79);
    var mid = match("Mercadona", "Leche 1L", 0.85);
    var expensive = match("Carrefour", "Leche 1L", 1.10);

    var comparison = ProductComparison.of("leche", List.of(expensive, cheap, mid));

    var sorted = comparison.sortedByPrice();
    assertThat(sorted).containsExactly(cheap, mid, expensive);
  }

  @Test
  void sortedByPrice_withTies_preservesAllMatches() {
    var a = match("Mercadona", "Pan 400g", 1.00);
    var b = match("LIDL", "Pan 400g", 1.00);

    var comparison = ProductComparison.of("pan", List.of(a, b));
    assertThat(comparison.sortedByPrice()).hasSize(2);
  }

  @Test
  void constructor_withBlankSearchTerm_throws() {
    assertThatThrownBy(() -> ProductComparison.of("  ", List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("searchTerm");
  }

  @Test
  void matches_areImmutable() {
    var comparison = ProductComparison.of("leche", List.of(match("Mercadona", "Leche", 0.85)));
    assertThatThrownBy(() -> comparison.matches().add(match("LIDL", "Leche", 0.79)))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
