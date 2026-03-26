package com.n1b3lung0.supermarkets.product.application.query;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductByIdQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductByIdUseCase;
import com.n1b3lung0.supermarkets.product.application.port.output.ProductQueryPort;
import com.n1b3lung0.supermarkets.product.domain.exception.ProductNotFoundException;
import com.n1b3lung0.supermarkets.product.domain.model.ProductId;
import java.util.Objects;

/** Returns full product detail including latest price. */
public class GetProductByIdHandler implements GetProductByIdUseCase {

  private final ProductQueryPort queryPort;

  public GetProductByIdHandler(ProductQueryPort queryPort) {
    this.queryPort = queryPort;
  }

  @Override
  public ProductDetailView execute(GetProductByIdQuery query) {
    Objects.requireNonNull(query, "query is required");
    var id = ProductId.of(query.productId());
    return queryPort.findDetailById(id).orElseThrow(() -> new ProductNotFoundException(id));
  }
}
