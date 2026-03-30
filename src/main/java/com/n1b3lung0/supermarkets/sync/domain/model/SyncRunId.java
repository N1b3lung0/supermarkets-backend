package com.n1b3lung0.supermarkets.sync.domain.model;

import java.util.UUID;

/** Value Object — internal UUID identifier for a SyncRun. */
public record SyncRunId(UUID value) {

  public SyncRunId {
    java.util.Objects.requireNonNull(value, "SyncRunId value is required");
  }

  public static SyncRunId of(UUID value) {
    return new SyncRunId(value);
  }

  public static SyncRunId generate() {
    return new SyncRunId(UUID.randomUUID());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
