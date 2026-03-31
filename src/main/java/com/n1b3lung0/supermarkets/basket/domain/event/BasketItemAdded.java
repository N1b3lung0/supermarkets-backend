package com.n1b3lung0.supermarkets.basket.domain.event;

import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;

public record BasketItemAdded(
    BasketId basketId, BasketItemId itemId, String productName, int quantity)
    implements BasketEvent {}
