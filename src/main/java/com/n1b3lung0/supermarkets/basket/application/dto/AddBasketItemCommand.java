package com.n1b3lung0.supermarkets.basket.application.dto;

import java.util.UUID;

public record AddBasketItemCommand(UUID basketId, String productName, int quantity) {}
