package com.n1b3lung0.supermarkets.supermarket.application.dto;

/** Command to register a new Supermarket in the system. */
public record RegisterSupermarketCommand(String name, String country) {}
