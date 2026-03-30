package com.n1b3lung0.supermarkets.basket.domain.event;

import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;

public record BasketCreated(BasketId basketId, String name) implements BasketEvent {}
