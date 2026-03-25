package com.n1b3lung0.supermarkets.supermarket.application.port.input.query;

import com.n1b3lung0.supermarkets.shared.domain.model.PageResponse;
import com.n1b3lung0.supermarkets.supermarket.application.dto.ListSupermarketsQuery;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketSummaryView;

/** Input port for the list-supermarkets use case. */
public interface ListSupermarketsUseCase {

  PageResponse<SupermarketSummaryView> execute(ListSupermarketsQuery query);
}
