package com.n1b3lung0.supermarkets.product.application.port.input.query;

import com.n1b3lung0.supermarkets.product.application.dto.ListProductsByCategoryQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductSummaryView;
import org.springframework.data.domain.Page;

/** Use case — paginated list of products in a given category. */
public interface ListProductsByCategoryUseCase {

  Page<ProductSummaryView> execute(ListProductsByCategoryQuery query);
}
