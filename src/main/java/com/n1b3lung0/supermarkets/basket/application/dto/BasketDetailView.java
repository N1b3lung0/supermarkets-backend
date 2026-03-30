package com.n1b3lung0.supermarkets.basket.application.dto;

import java.util.List;
import java.util.UUID;

public record BasketDetailView(UUID id, String name, List<BasketItemView> items) {}
