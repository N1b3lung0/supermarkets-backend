package com.n1b3lung0.supermarkets.basket.application.port.input.query;

import com.n1b3lung0.supermarkets.basket.application.dto.BasketComparisonView;
import com.n1b3lung0.supermarkets.basket.application.dto.CompareBasketQuery;

public interface CompareBasketUseCase {
  BasketComparisonView execute(CompareBasketQuery query);
}
