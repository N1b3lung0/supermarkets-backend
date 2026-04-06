package com.n1b3lung0.supermarkets.product.infrastructure.adapter.input.event;

import com.n1b3lung0.supermarkets.comparison.infrastructure.adapter.cache.CachingCompareProductsByNameUseCase;
import com.n1b3lung0.supermarkets.product.domain.event.ProductDeactivated;
import com.n1b3lung0.supermarkets.product.domain.event.ProductSynced;
import com.n1b3lung0.supermarkets.product.infrastructure.adapter.cache.CachingGetProductByIdUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

/**
 * Listens to product domain events published via {@link
 * org.springframework.context.ApplicationEventPublisher} and evicts the relevant cache entries.
 *
 * <ul>
 *   <li>{@link ProductSynced} → evict {@code product:{id}} + clear all {@code compare} entries
 *       (prices may have changed)
 *   <li>{@link ProductDeactivated} → evict {@code product:{id}} only
 * </ul>
 */
public class ProductCacheEvictionListener {

  private static final Logger log = LoggerFactory.getLogger(ProductCacheEvictionListener.class);

  private final CachingGetProductByIdUseCase productCache;
  private final CachingCompareProductsByNameUseCase compareCache;

  public ProductCacheEvictionListener(
      CachingGetProductByIdUseCase productCache, CachingCompareProductsByNameUseCase compareCache) {
    this.productCache = productCache;
    this.compareCache = compareCache;
  }

  @EventListener
  public void onProductSynced(ProductSynced event) {
    var productId = event.productId().value();
    productCache.evict(productId);
    compareCache.evictAll();
    log.debug("Cache evicted after ProductSynced: product={}, compare=ALL", productId);
  }

  @EventListener
  public void onProductDeactivated(ProductDeactivated event) {
    var productId = event.productId().value();
    productCache.evict(productId);
    log.debug("Cache evicted after ProductDeactivated: product={}", productId);
  }
}
