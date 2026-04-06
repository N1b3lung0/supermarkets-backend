package com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.cache;

import com.n1b3lung0.supermarkets.comparison.application.dto.CompareProductsByNameQuery;
import com.n1b3lung0.supermarkets.comparison.application.dto.ProductComparisonView;
import com.n1b3lung0.supermarkets.comparison.application.port.input.query.CompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.shared.infrastructure.config.CacheConfig;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.cache.CacheManager;

/**
 * Infrastructure decorator that adds caching to {@link CompareProductsByNameUseCase}.
 *
 * <p>Cache key: {@code {searchTerm}:{sortedSupermarketIds}} — IDs are sorted before joining to
 * produce a stable key regardless of request order.
 *
 * <p>Keeping cache logic in infrastructure keeps the application handler annotation-free,
 * consistent with the project's hexagonal architecture rules.
 */
public class CachingCompareProductsByNameUseCase implements CompareProductsByNameUseCase {

  private final CompareProductsByNameUseCase delegate;
  private final CacheManager cacheManager;

  public CachingCompareProductsByNameUseCase(
      CompareProductsByNameUseCase delegate, CacheManager cacheManager) {
    this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    this.cacheManager = Objects.requireNonNull(cacheManager, "cacheManager is required");
  }

  @Override
  public ProductComparisonView execute(CompareProductsByNameQuery query) {
    Objects.requireNonNull(query, "query is required");

    var cache = cacheManager.getCache(CacheConfig.CACHE_COMPARE);
    if (cache == null) {
      return delegate.execute(query);
    }

    var key = buildKey(query);
    var cached = cache.get(key, ProductComparisonView.class);
    if (cached != null) {
      return cached;
    }

    var result = delegate.execute(query);
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
