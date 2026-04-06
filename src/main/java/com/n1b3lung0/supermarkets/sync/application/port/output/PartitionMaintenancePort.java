package com.n1b3lung0.supermarkets.sync.application.port.output;

/**
 * Output port — ensures the {@code product_prices} partition for the next calendar month exists
 * before a sync starts, preventing "no partition found" errors on INSERT.
 */
public interface PartitionMaintenancePort {

  /** Creates the next month's partition if it does not already exist. */
  void ensureNextMonthPartitionExists();
}
