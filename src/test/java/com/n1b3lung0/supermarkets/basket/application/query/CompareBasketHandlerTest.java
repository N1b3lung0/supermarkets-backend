package com.n1b3lung0.supermarkets.basket.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.n1b3lung0.supermarkets.basket.application.dto.CompareBasketQuery;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.Basket;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.comparison.application.port.output.ProductComparisonQueryPort;
import com.n1b3lung0.supermarkets.comparison.domain.model.ProductMatch;
import com.n1b3lung0.supermarkets.shared.domain.model.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Step 71 — unit tests for CompareBasketHandler. */
@ExtendWith(MockitoExtension.class)
class CompareBasketHandlerTest {

  @Mock private BasketRepositoryPort basketRepository;
  @Mock private ProductComparisonQueryPort comparisonQueryPort;

  private CompareBasketHandler handler() {
    return new CompareBasketHandler(basketRepository, comparisonQueryPort);
  }

  private static final UUID SM_MERCADONA = UUID.randomUUID();
  private static final UUID SM_LIDL = UUID.randomUUID();

  private ProductMatch match(UUID supermarketId, String smName, String productName, double price) {
    return new ProductMatch(
        UUID.randomUUID(),
        supermarketId,
        smName,
        productName,
        Money.ofEur(BigDecimal.valueOf(price)),
        null,
        null,
        null,
        Instant.now());
  }

  @Test
  void execute_basketNotFound_throws() {
    given(basketRepository.findById(any())).willReturn(Optional.empty());
    assertThatThrownBy(() -> handler().execute(new CompareBasketQuery(UUID.randomUUID())))
        .isInstanceOf(BasketNotFoundException.class);
  }

  @Test
  void execute_threeItems_twoSupermarkets_correctTotalsAndCheapest() {
    var basket = Basket.create("Mi cesta");
    basket.addItem("Leche 1L", 2);
    basket.addItem("Aceite oliva 1L", 1);
    basket.addItem("Pan 400g", 3);
    given(basketRepository.findById(BasketId.of(basket.getId().value())))
        .willReturn(Optional.of(basket));

    // Mercadona: leche=0.89, aceite=3.50, pan=1.10
    // LIDL:      leche=0.69, aceite=2.99, pan=0.89
    given(comparisonQueryPort.findMatchesByName(eq("Leche 1L"), any()))
        .willReturn(
            List.of(
                match(SM_MERCADONA, "Mercadona", "Leche entera 1L", 0.89),
                match(SM_LIDL, "LIDL", "Leche fresca 1L", 0.69)));
    given(comparisonQueryPort.findMatchesByName(eq("Aceite oliva 1L"), any()))
        .willReturn(
            List.of(
                match(SM_MERCADONA, "Mercadona", "Aceite oliva virgen 1L", 3.50),
                match(SM_LIDL, "LIDL", "Aceite oliva 1L", 2.99)));
    given(comparisonQueryPort.findMatchesByName(eq("Pan 400g"), any()))
        .willReturn(
            List.of(
                match(SM_MERCADONA, "Mercadona", "Pan bimbo 400g", 1.10),
                match(SM_LIDL, "LIDL", "Pan molde 400g", 0.89)));

    var result = handler().execute(new CompareBasketQuery(basket.getId().value()));

    // Mercadona: 0.89*2 + 3.50*1 + 1.10*3 = 1.78 + 3.50 + 3.30 = 8.58
    // LIDL:      0.69*2 + 2.99*1 + 0.89*3 = 1.38 + 2.99 + 2.67 = 7.04
    assertThat(result.perSupermarket()).hasSize(2);
    // sorted by total ascending → LIDL first
    assertThat(result.perSupermarket().get(0).supermarketId()).isEqualTo(SM_LIDL);
    assertThat(result.perSupermarket().get(0).totalCost())
        .isEqualByComparingTo(BigDecimal.valueOf(7.04));
    assertThat(result.cheapestSupermarketId()).isEqualTo(SM_LIDL);
    assertThat(result.cheapestSupermarketName()).isEqualTo("LIDL");
  }

  @Test
  void execute_emptyBasket_returnsEmptyPerSupermarket() {
    var basket = Basket.create("Vacía");
    given(basketRepository.findById(BasketId.of(basket.getId().value())))
        .willReturn(Optional.of(basket));

    var result = handler().execute(new CompareBasketQuery(basket.getId().value()));

    assertThat(result.perSupermarket()).isEmpty();
    assertThat(result.cheapestSupermarketId()).isNull();
  }

  @Test
  void execute_noMatchesForAnyItem_returnsEmptyPerSupermarket() {
    var basket = Basket.create("Sin resultados");
    basket.addItem("Producto inexistente", 1);
    given(basketRepository.findById(BasketId.of(basket.getId().value())))
        .willReturn(Optional.of(basket));
    given(comparisonQueryPort.findMatchesByName(any(), any())).willReturn(List.of());

    var result = handler().execute(new CompareBasketQuery(basket.getId().value()));

    assertThat(result.perSupermarket()).isEmpty();
    assertThat(result.cheapestSupermarketId()).isNull();
  }
}
