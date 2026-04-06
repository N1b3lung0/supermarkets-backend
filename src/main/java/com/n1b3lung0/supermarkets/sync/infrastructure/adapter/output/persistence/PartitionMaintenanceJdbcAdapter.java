package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.sync.application.port.output.PartitionMaintenancePort;
import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Creates the next month's {@code product_prices} partition using a DDL statement if it doesn't
 * exist yet. Called at the start of each sync to avoid "no partition of relation found" errors.
 */
public class PartitionMaintenanceJdbcAdapter implements PartitionMaintenancePort {

  private static final Logger log = LoggerFactory.getLogger(PartitionMaintenanceJdbcAdapter.class);

  private final JdbcClient jdbcClient;

  public PartitionMaintenanceJdbcAdapter(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public void ensureNextMonthPartitionExists() {
    var nextMonth = YearMonth.now().plusMonths(1);
    var partitionName = "product_prices_" + nextMonth.getYear() + "_" + nextMonth.getMonthValue();
    var fromDate = nextMonth.atDay(1);
    var toDate = nextMonth.plusMonths(1).atDay(1);

    // Use DO block so we can check pg_class without raising an error if it already exists
    var sql =
        """
        DO $$
        BEGIN
          IF NOT EXISTS (
            SELECT 1 FROM pg_class WHERE relname = '%s'
          ) THEN
            EXECUTE format(
              'CREATE TABLE %s PARTITION OF product_prices FOR VALUES FROM (''%s'') TO (''%s'')',
              '%s', '%s', '%s', '%s'
            );
          END IF;
        END $$;
        """
            .formatted(
                partitionName,
                partitionName,
                fromDate,
                toDate,
                partitionName,
                partitionName,
                fromDate,
                toDate);

    jdbcClient.sql(sql).update();
    log.debug(
        "Partition maintenance: ensured partition {} exists (range [{}, {}))",
        partitionName,
        fromDate,
        toDate);
  }
}
