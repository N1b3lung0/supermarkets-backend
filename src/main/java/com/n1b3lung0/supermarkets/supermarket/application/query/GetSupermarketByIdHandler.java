package com.n1b3lung0.supermarkets.supermarket.application.query;

import com.n1b3lung0.supermarkets.supermarket.application.dto.GetSupermarketByIdQuery;
import com.n1b3lung0.supermarkets.supermarket.application.dto.SupermarketDetailView;
import com.n1b3lung0.supermarkets.supermarket.application.port.input.query.GetSupermarketByIdUseCase;
import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketQueryPort;
import com.n1b3lung0.supermarkets.supermarket.domain.exception.SupermarketNotFoundException;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.Objects;

/** Handles retrieval of a single Supermarket by ID. */
public class GetSupermarketByIdHandler implements GetSupermarketByIdUseCase {

  private final SupermarketQueryPort queryPort;

  public GetSupermarketByIdHandler(SupermarketQueryPort queryPort) {
    this.queryPort = Objects.requireNonNull(queryPort, "queryPort is required");
  }

  @Override
  public SupermarketDetailView execute(GetSupermarketByIdQuery query) {
    Objects.requireNonNull(query, "query is required");
    var id = SupermarketId.of(query.id());
    return queryPort.findDetailById(id).orElseThrow(() -> new SupermarketNotFoundException(id));
  }
}
