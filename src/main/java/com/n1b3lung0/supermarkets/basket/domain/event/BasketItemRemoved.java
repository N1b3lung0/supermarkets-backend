package com.n1b3lung0.supermarkets.basket.domain.event;

import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;

public record BasketItemRemoved(BasketId basketId, BasketItemId itemId) implements BasketEvent {}
