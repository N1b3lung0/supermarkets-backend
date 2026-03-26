package com.n1b3lung0.supermarkets.product.application.port.input.query;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductPriceHistoryQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductPriceView;
import org.springframework.data.domain.Page;

/** Use case — paginated price history for a product, newest first. */
public interface GetProductPriceHistoryUseCase {

  Page<ProductPriceView> execute(GetProductPriceHistoryQuery query);
}
