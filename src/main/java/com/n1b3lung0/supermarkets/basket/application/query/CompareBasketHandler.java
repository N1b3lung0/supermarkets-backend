package com.n1b3lung0.supermarkets.basket.application.query;

import com.n1b3lung0.supermarkets.basket.application.dto.BasketComparisonView;
import com.n1b3lung0.supermarkets.basket.application.dto.BasketItemMatchView;
import com.n1b3lung0.supermarkets.basket.application.dto.CompareBasketQuery;
import com.n1b3lung0.supermarkets.basket.application.dto.SupermarketBasketCost;
import com.n1b3lung0.supermarkets.basket.application.port.input.query.CompareBasketUseCase;
import com.n1b3lung0.supermarkets.basket.application.port.output.BasketRepositoryPort;
import com.n1b3lung0.supermarkets.basket.domain.exception.BasketNotFoundException;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItem;
import com.n1b3lung0.supermarkets.comparison.application.port.output.ProductComparisonQueryPort;
import com.n1b3lung0.supermarkets.comparison.domain.model.ProductMatch;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Compares the total cost of a basket across all supermarkets.
 *
 * <p>Strategy: for each basket item, search for matching products via name across all supermarkets.
 * Group results by supermarket, pick the cheapest match per supermarket per item, multiply by
 * quantity, and sum to get the total cost per supermarket.
 *
 * <p>Cross-context dependency: uses {@link ProductComparisonQueryPort} (owned by the Comparison BC)
 * as an output port — no direct class coupling to the Comparison application layer.
 */
public class CompareBasketHandler implements CompareBasketUseCase {

  private final BasketRepositoryPort basketRepository;
  private final ProductComparisonQueryPort comparisonQueryPort;

  public CompareBasketHandler(
      BasketRepositoryPort basketRepository, ProductComparisonQueryPort comparisonQueryPort) {
    this.basketRepository = basketRepository;
    this.comparisonQueryPort = comparisonQueryPort;
  }

  @Override
  public BasketComparisonView execute(CompareBasketQuery query) {
    Objects.requireNonNull(query, "query is required");

    var basket =
        basketRepository
            .findById(BasketId.of(query.basketId()))
            .orElseThrow(() -> new BasketNotFoundException(query.basketId().toString()));

    // Map<supermarketId, Map<basketItemName, cheapest ProductMatch>>
    Map<UUID, Map<String, ProductMatch>> bestMatchBySupermarket = new LinkedHashMap<>();
    Map<UUID, String> supermarketNames = new LinkedHashMap<>();

    for (BasketItem item : basket.getItems()) {
      var matches = comparisonQueryPort.findMatchesByName(item.getProductName(), List.of());
      for (ProductMatch match : matches) {
        var smId = match.supermarketId();
        supermarketNames.putIfAbsent(smId, match.supermarketName());
        bestMatchBySupermarket
            .computeIfAbsent(smId, k -> new LinkedHashMap<>())
            .merge(
                item.getProductName(),
                match,
                (existing, candidate) ->
                    candidate.unitPriceAmount().compareTo(existing.unitPriceAmount()) < 0
                        ? candidate
                        : existing);
      }
    }

    List<SupermarketBasketCost> perSupermarket = new ArrayList<>();

    for (var entry : bestMatchBySupermarket.entrySet()) {
      var smId = entry.getKey();
      var smName = supermarketNames.get(smId);
      var itemMatches = new ArrayList<BasketItemMatchView>();
      var total = BigDecimal.ZERO;

      for (BasketItem item : basket.getItems()) {
        var match = entry.getValue().get(item.getProductName());
        if (match != null) {
          var lineTotal = match.unitPriceAmount().multiply(BigDecimal.valueOf(item.getQuantity()));
          total = total.add(lineTotal);
          itemMatches.add(
              new BasketItemMatchView(
                  item.getProductName(),
                  item.getQuantity(),
                  match.productName(),
                  match.unitPriceAmount(),
                  lineTotal));
        }
      }

      perSupermarket.add(new SupermarketBasketCost(smId, smName, total, itemMatches));
    }

    // Sort by total cost ascending
    perSupermarket.sort(Comparator.comparing(SupermarketBasketCost::totalCost));

    var cheapest = perSupermarket.isEmpty() ? null : perSupermarket.get(0);

    return new BasketComparisonView(
        basket.getId().value(),
        basket.getName(),
        perSupermarket,
        cheapest != null ? cheapest.supermarketId() : null,
        cheapest != null ? cheapest.supermarketName() : null);
  }
}
