package com.n1b3lung0.supermarkets.shared.infrastructure.config;

import java.time.Duration;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Configures per-cache TTLs for the Redis-backed Spring Cache.
 *
 * <ul>
 *   <li>{@code compare} — 1 hour (comparison queries are expensive and change only after a sync)
 *   <li>{@code product} — 30 minutes (product details change less often than prices)
 * </ul>
 *
 * <p>In the {@code test} profile, {@code spring.cache.type=simple} is set in {@code
 * application-test.yaml}, so this bean is not activated at test time.
 */
@Configuration
public class CacheConfig {

  /** Cache name for cross-supermarket product comparison results. */
  public static final String CACHE_COMPARE = "compare";

  /** Cache name for single-product detail lookups. */
  public static final String CACHE_PRODUCT = "product";

  @Bean
  @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    var defaultConfig = RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues();

    var cacheConfigs =
        Map.of(
            CACHE_COMPARE,
            defaultConfig.entryTtl(Duration.ofHours(1)),
            CACHE_PRODUCT,
            defaultConfig.entryTtl(Duration.ofMinutes(30)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigs)
        .build();
  }
}
