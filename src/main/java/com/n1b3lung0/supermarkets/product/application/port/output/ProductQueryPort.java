package com.n1b3lung0.supermarkets.product.application.port.output;

import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;
import com.n1b3lung0.supermarkets.product.application.dto.ProductSummaryView;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Read-side port — query operations for Product. */
public interface ProductQueryPort {

  Optional<ProductDetailView> findDetailById(ProductId id);

  Page<ProductSummaryView> findSummariesByCategory(CategoryId categoryId, Pageable pageable);

  Page<ProductSummaryView> findSummariesBySupermarket(
      SupermarketId supermarketId, Pageable pageable);
}
