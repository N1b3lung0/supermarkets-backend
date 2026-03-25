package com.n1b3lung0.supermarkets.supermarket.application.query;

import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
import com.n1b3lung0.supermarkets.supermarket.application.dto.ListSupermarketsQuery;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketSummaryView;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.query.ListSupermarketsUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketQueryPort;
import java.util.Objects;

/** Handles paginated listing of all active Supermarkets. */
public class ListSupermarketsHandler implements ListSupermarketsUseCase {

  private final SupermarketQueryPort queryPort;

  public ListSupermarketsHandler(SupermarketQueryPort queryPort) {
    this.queryPort = Objects.requireNonNull(queryPort, "queryPort is required");
  }

  @Override
  public PageResponse<SupermarketSummaryView> execute(ListSupermarketsQuery query) {
    Objects.requireNonNull(query, "query is required");
    return PageResponse.from(queryPort.findAll(query.pageable()));
  }
}
