package com.n1b3lung0.supermarkets.supermarket.application.port.input.query;

import com.n1b3lung0.supermarkets.supermarket.application.dto.GetSupermarketByIdQuery;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketDetailView;

/** Input port for the get-supermarket-by-id use case. */
public interface GetSupermarketByIdUseCase {

  SupermarketDetailView execute(GetSupermarketByIdQuery query);
}
