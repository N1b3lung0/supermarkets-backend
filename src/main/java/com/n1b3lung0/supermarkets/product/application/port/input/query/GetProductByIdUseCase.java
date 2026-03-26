package com.n1b3lung0.supermarkets.product.application.port.input.query;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductByIdQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;

/** Use case — retrieve full product detail by id. */
public interface GetProductByIdUseCase {

  ProductDetailView execute(GetProductByIdQuery query);
}
