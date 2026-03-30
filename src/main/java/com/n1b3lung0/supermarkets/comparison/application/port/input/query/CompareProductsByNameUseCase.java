package com.n1b3lung0.supermarkets.comparison.application.port.input.query;

import com.n1b3lung0.supermarkets.comparison.application.dto.CompareProductsByNameQuery;
import com.n1b3lung0.supermarkets.comparison.application.dto.ProductComparisonView;

/** Input port — compare products across supermarkets by name. */
public interface CompareProductsByNameUseCase {

  ProductComparisonView execute(CompareProductsByNameQuery query);
}
