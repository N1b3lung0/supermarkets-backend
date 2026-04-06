package com.n1b3lung0.supermarkets.lidl.infrastructure.config.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Externalized configuration for the LIDL scraper HTTP client. */
@ConfigurationProperties(prefix = "app.scraper.lidl")
public record LidlScraperProperties(String baseUrl, Duration requestTimeout) {}
