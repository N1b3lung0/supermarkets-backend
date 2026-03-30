CREATE TABLE sync_runs
(
    id                   UUID         NOT NULL PRIMARY KEY,
    supermarket_id       UUID         NOT NULL REFERENCES supermarkets (id),
    started_at           TIMESTAMPTZ  NOT NULL,
    finished_at          TIMESTAMPTZ,
    status               VARCHAR(20)  NOT NULL,
    categories_synced    INTEGER      NOT NULL DEFAULT 0,
    products_synced      INTEGER      NOT NULL DEFAULT 0,
    products_deactivated INTEGER      NOT NULL DEFAULT 0,
    error_message        TEXT
);

CREATE INDEX idx_sync_runs_supermarket_id ON sync_runs (supermarket_id);
CREATE INDEX idx_sync_runs_started_at ON sync_runs (started_at DESC);

