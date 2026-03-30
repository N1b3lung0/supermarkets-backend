package com.n1b3lung0.supermarkets.basket.application.dto;

import java.util.UUID;

public record UpdateBasketItemQuantityCommand(UUID basketId, UUID itemId, int quantity) {}
