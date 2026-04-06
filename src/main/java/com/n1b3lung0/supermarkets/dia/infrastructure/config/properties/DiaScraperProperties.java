package com.n1b3lung0.supermarkets.dia.infrastructure.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Externalized configuration for the DIA scraper HTTP client. */
@ConfigurationProperties(prefix = "app.scraper.dia")
public record DiaScraperProperties(String baseUrl, Duration requestTimeout) {}
