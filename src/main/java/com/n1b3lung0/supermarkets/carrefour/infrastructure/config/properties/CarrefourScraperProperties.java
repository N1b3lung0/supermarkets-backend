package com.n1b3lung0.supermarkets.carrefour.infrastructure.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Externalized configuration for the Carrefour scraper HTTP client. */
@ConfigurationProperties(prefix = "app.scraper.carrefour")
public record CarrefourScraperProperties(String baseUrl, Duration requestTimeout) {}
