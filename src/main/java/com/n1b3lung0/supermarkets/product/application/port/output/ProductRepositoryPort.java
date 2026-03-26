package com.n1b3lung0.supermarkets.product.application.port.output;

import com.n1b3lung0.supermarkets.product.domain.model.ExternalProductId;
import com.n1b3lung0.supermarkets.product.domain.model.Product;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.List;
import java.util.Optional;

/** Write-side port — persistence operations for Product. */
public interface ProductRepositoryPort {

  void save(Product product);

  Optional<Product> findById(ProductId id);

  Optional<Product> findByExternalIdAndSupermarket(
      ExternalProductId externalId, SupermarketId supermarketId);

  /** Returns all external IDs for active products belonging to the given supermarket. */
  List<String> findActiveExternalIdsBySupermarket(SupermarketId supermarketId);

  void deleteById(ProductId id);
}
