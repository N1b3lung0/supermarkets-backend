package com.n1b3lung0.supermarkets.alcampo.infrastructure.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Externalized configuration for the Alcampo scraper HTTP client. */
@ConfigurationProperties(prefix = "app.scraper.alcampo")
public record AlcampoScraperProperties(String baseUrl, Duration requestTimeout) {}
