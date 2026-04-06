package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.sync.application.port.output.LatestPricesRefreshPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Refreshes the {@code latest_product_prices} materialized view concurrently after each sync. Using
 * {@code CONCURRENTLY} avoids locking the view during refresh (requires a unique index on {@code
 * product_id}, created in V14).
 */
public class LatestPricesRefreshJdbcAdapter implements LatestPricesRefreshPort {

  private static final Logger log = LoggerFactory.getLogger(LatestPricesRefreshJdbcAdapter.class);

  private final JdbcClient jdbcClient;

  public LatestPricesRefreshJdbcAdapter(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public void refresh() {
    log.info("Refreshing materialized view latest_product_prices...");
    jdbcClient.sql("REFRESH MATERIALIZED VIEW CONCURRENTLY latest_product_prices").update();
    log.info("Materialized view latest_product_prices refreshed.");
  }
}
