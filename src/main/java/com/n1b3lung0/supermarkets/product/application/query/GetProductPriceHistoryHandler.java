package com.n1b3lung0.supermarkets.product.application.query;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductPriceHistoryQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductPriceView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductPriceHistoryUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductPriceQueryPort;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import java.util.Objects;
import org.springframework.data.domain.Page;

/** Returns paginated price history for a product, newest first. */
public class GetProductPriceHistoryHandler implements GetProductPriceHistoryUseCase {

  private final ProductPriceQueryPort queryPort;

  public GetProductPriceHistoryHandler(ProductPriceQueryPort queryPort) {
    this.queryPort = queryPort;
  }

  @Override
  public Page<ProductPriceView> execute(GetProductPriceHistoryQuery query) {
    Objects.requireNonNull(query, "query is required");
    return queryPort.findHistoryByProductId(ProductId.of(query.productId()), query.pageable());
  }
}
