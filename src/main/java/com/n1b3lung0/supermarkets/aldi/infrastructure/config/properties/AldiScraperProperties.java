package com.n1b3lung0.supermarkets.aldi.infrastructure.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Externalized configuration for the ALDI scraper HTTP client. */
@ConfigurationProperties(prefix = "app.scraper.aldi")
public record AldiScraperProperties(String baseUrl, Duration requestTimeout) {}
