package com.n1b3lung0.supermarkets.product.infrastructure.adapter.cache;

import com.n1b3lung0.supermarkets.product.application.dto.GetProductByIdQuery;
import com.n1b3lung0.supermarkets.product.application.dto.ProductDetailView;
import com.n1b3lung0.supermarkets.product.application.port.input.query.GetProductByIdUseCase;
import com.n1b3lung0.supermarkets.shared.infrastructure.config.CacheConfig;
import java.util.Objects;
import org.springframework.cache.CacheManager;

/**
 * Infrastructure decorator that adds caching to {@link GetProductByIdUseCase}.
 *
 * <p>Cache key: {@code {productId}} — evicted when a {@code ProductSynced} or {@code
 * ProductDeactivated} event is published.
 *
 * <p>Keeps the application handler annotation-free (hexagonal architecture rule).
 */
public class CachingGetProductByIdUseCase implements GetProductByIdUseCase {

  private final GetProductByIdUseCase delegate;
  private final CacheManager cacheManager;

  public CachingGetProductByIdUseCase(GetProductByIdUseCase delegate, CacheManager cacheManager) {
    this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    this.cacheManager = Objects.requireNonNull(cacheManager, "cacheManager is required");
  }

  @Override
  public ProductDetailView execute(GetProductByIdQuery query) {
    Objects.requireNonNull(query, "query is required");

    var cache = cacheManager.getCache(CacheConfig.CACHE_PRODUCT);
    if (cache == null) {
      return delegate.execute(query);
    }

    var key = query.productId().toString();
    var cached = cache.get(key, ProductDetailView.class);
    if (cached != null) {
      return cached;
    }

    var result = delegate.execute(query);
    cache.put(key, result);
    return result;
  }

  /** Evicts the cache entry for a specific product (called on sync / deactivation events). */
  public void evict(java.util.UUID productId) {
    var cache = cacheManager.getCache(CacheConfig.CACHE_PRODUCT);
    if (cache != null) {
      cache.evict(productId.toString());
    }
  }
}
