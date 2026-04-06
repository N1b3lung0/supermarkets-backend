package com.n1b3lung0.supermarkets.sync.application.port.output;

/**
 * Output port — refreshes the {@code latest_product_prices} materialized view so that comparison
 * queries reflect the prices recorded during the most recent sync.
 */
public interface LatestPricesRefreshPort {

  /** Executes {@code REFRESH MATERIALIZED VIEW CONCURRENTLY latest_product_prices}. */
  void refresh();
}
