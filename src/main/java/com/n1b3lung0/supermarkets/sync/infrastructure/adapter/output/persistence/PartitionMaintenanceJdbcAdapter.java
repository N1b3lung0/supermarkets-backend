package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.output.persistence;

import com.n1b3lung0.supermarkets.sync.application.port.output.PartitionMaintenancePort;
import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Creates the next month's {@code product_prices} partition using a plain DDL statement if it isn't
 * already covered. Called at the start of each sync to avoid "no partition found" errors.
 *
 * <p>Uses {@link JdbcTemplate#execute(String)} (plain {@code Statement.execute}) rather than {@code
 * JdbcClient.update()} because DDL statements must not be executed as JDBC prepared statements in
 * PostgreSQL.
 *
 * <p>Two idempotency cases are handled in Java (not PL/pgSQL) because PostgreSQL validates
 * partition bounds during semantic analysis — before PL/pgSQL's {@code EXCEPTION WHEN} handler can
 * catch them:
 *
 * <ul>
 *   <li>{@code "already exists"} (42P07 duplicate_table) — partition with this name exists.
 *   <li>{@code "would overlap"} (42P16 invalid_table_definition) — another partition (e.g. a
 *       Flyway-created quarterly one) already covers the target date range.
 * </ul>
 */
public class PartitionMaintenanceJdbcAdapter implements PartitionMaintenancePort {

  private static final Logger log = LoggerFactory.getLogger(PartitionMaintenanceJdbcAdapter.class);

  private final JdbcTemplate jdbcTemplate;

  public PartitionMaintenanceJdbcAdapter(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void ensureNextMonthPartitionExists() {
    var nextMonth = YearMonth.now().plusMonths(1);
    var partitionName = "product_prices_" + nextMonth.getYear() + "_" + nextMonth.getMonthValue();
    var fromDate = nextMonth.atDay(1);
    var toDate = nextMonth.plusMonths(1).atDay(1);

    var sql =
        "CREATE TABLE %s PARTITION OF product_prices FOR VALUES FROM ('%s') TO ('%s')"
            .formatted(partitionName, fromDate, toDate);

    try {
      jdbcTemplate.execute(sql);
      log.debug(
          "Partition maintenance: created partition {} (range [{}, {}))",
          partitionName,
          fromDate,
          toDate);
    } catch (DataAccessException ex) {
      var msg = ex.getMostSpecificCause().getMessage();
      if (msg != null && (msg.contains("already exists") || msg.contains("would overlap"))) {
        log.debug(
            "Partition maintenance: partition {} already covered, skipping creation — {}",
            partitionName,
            msg);
      } else {
        throw ex;
      }
    }
  }
}
