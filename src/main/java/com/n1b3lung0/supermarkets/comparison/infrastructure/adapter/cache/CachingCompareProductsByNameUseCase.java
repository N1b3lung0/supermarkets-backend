package com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.cache;

import com.n1b3lung0.supermarkets.comparison.application.dto.CompareProductsByNameQuery;
import com.n1b3lung0.supermarkets.comparison.application.dto.ProductComparisonView;
import com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.shared.infrastructure.config.CacheConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.cache.CacheManager;

/**
 * Infrastructure decorator that adds caching and business metrics to {@link
 * CompareProductsByNameUseCase}.
 *
 * <p>Cache key: {@code {searchTerm}:{sortedSupermarketIds}} — IDs are sorted before joining to
 * produce a stable key regardless of request order.
 *
 * <p>Metrics recorded:
 *
 * <ul>
 *   <li>{@code comparisons.executed.total} — counter incremented on every invocation
 *   <li>{@code comparisons.results.count} — distribution summary of matches per search
 * </ul>
 */
public class CachingCompareProductsByNameUseCase implements CompareProductsByNameUseCase {

  private final CompareProductsByNameUseCase delegate;
  private final CacheManager cacheManager;
  private final Counter executedCounter;
  private final DistributionSummary resultsSummary;

  public CachingCompareProductsByNameUseCase(
      CompareProductsByNameUseCase delegate,
      CacheManager cacheManager,
      MeterRegistry meterRegistry) {
    this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    this.cacheManager = Objects.requireNonNull(cacheManager, "cacheManager is required");
    this.executedCounter =
        Counter.builder("comparisons.executed.total")
            .description("Total number of product comparison queries executed")
            .register(meterRegistry);
    this.resultsSummary =
        DistributionSummary.builder("comparisons.results.count")
            .description("Number of product matches returned per comparison query")
            .register(meterRegistry);
  }

  @Override
  public ProductComparisonView execute(CompareProductsByNameQuery query) {
    Objects.requireNonNull(query, "query is required");

    executedCounter.increment();

    var cache = cacheManager.getCache(CacheConfig.CACHE_COMPARE);
    if (cache == null) {
      var result = delegate.execute(query);
      resultsSummary.record(result.matches().size());
      return result;
    }

    var key = buildKey(query);
    var cached = cache.get(key, ProductComparisonView.class);
    if (cached != null) {
      resultsSummary.record(cached.matches().size());
      return cached;
    }

    var result = delegate.execute(query);
    resultsSummary.record(result.matches().size());
    cache.put(key, result);
    return result;
  }

  /** Evicts all entries from the compare cache (called after a sync). */
  public void evictAll() {
    var cache = cacheManager.getCache(CacheConfig.CACHE_COMPARE);
    if (cache != null) {
      cache.clear();
    }
  }

  private static String buildKey(CompareProductsByNameQuery query) {
    var ids =
        query.supermarketIds() == null
            ? List.<UUID>of()
            : new java.util.ArrayList<>(query.supermarketIds());
    ids.sort(UUID::compareTo);
    return query.searchTerm().toLowerCase() + ":" + ids;
  }
}
