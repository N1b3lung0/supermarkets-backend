package com.n1b3lung0.supermarkets.supermarket.application.port.output;

import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketDetailView;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketSummaryView;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Output port for read-only queries on Supermarket. Uses projections, never the domain model. */
public interface SupermarketQueryPort {

  Optional<SupermarketDetailView> findDetailById(SupermarketId id);

  Page<SupermarketSummaryView> findAll(Pageable pageable);
}
