package com.n1b3lung0.supermarkets.product.application.port.output;

import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.product.domain.model.ProductPrice;
import java.util.Optional;

/** Write-side port — persistence operations for ProductPrice. */
public interface ProductPriceRepositoryPort {

  void save(ProductPrice productPrice);

  Optional<ProductPrice> findLatestByProductId(ProductId productId);
}
