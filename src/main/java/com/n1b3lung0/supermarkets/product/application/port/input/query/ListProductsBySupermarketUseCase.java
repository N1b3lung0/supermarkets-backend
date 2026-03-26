package com.n1b3lung0.supermarkets.product.application.port.input.query;

import com.n1b3lung0.supermarkets.product.application.dto.ListProductsBySupermarketQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductSummaryView;
import org.springframework.data.domain.Page;

/** Use case — paginated list of products in a given supermarket. */
public interface ListProductsBySupermarketUseCase {

  Page<ProductSummaryView> execute(ListProductsBySupermarketQuery query);
}
