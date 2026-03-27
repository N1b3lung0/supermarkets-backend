package com.n1b3lung0.supermarkets.mercadona.infrastructure.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Externalized configuration for the Mercadona scraper HTTP client. */
@ConfigurationProperties(prefix = "app.scraper.mercadona")
public record MercadonaScraperProperties(String baseUrl, Duration requestTimeout) {}
