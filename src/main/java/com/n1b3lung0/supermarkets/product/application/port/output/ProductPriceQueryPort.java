package com.n1b3lung0.supermarkets.product.application.port.output;

import com.n1b3lung0.supermarkets.product.application.dto.ProductPriceView;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Read-side port — query operations for ProductPrice history. */
public interface ProductPriceQueryPort {

  Page<ProductPriceView> findHistoryByProductId(ProductId productId, Pageable pageable);
}
