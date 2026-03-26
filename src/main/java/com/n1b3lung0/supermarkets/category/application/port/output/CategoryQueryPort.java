package com.n1b3lung0.supermarkets.category.application.port.output;

import com.n1b3lung0.supermarkets.category.application.dto.CategoryDetailView;
import com.n1b3lung0.supermarkets.category.application.dto.CategorySummaryView;
import com.n1b3lung0.supermarkets.category.domain.model.CategoryId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Read-side port — projections for Category. */
public interface CategoryQueryPort {

  Optional<CategoryDetailView> findDetailById(CategoryId id);

  Page<CategorySummaryView> findAll(UUID supermarketId, String levelType, Pageable pageable);
}
