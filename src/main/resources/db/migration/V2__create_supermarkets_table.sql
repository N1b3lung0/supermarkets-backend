-- V2: Create supermarkets table
CREATE TABLE supermarkets
(
    id         UUID        NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    country    CHAR(2)     NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ          DEFAULT NULL,

    CONSTRAINT pk_supermarkets PRIMARY KEY (id),
    CONSTRAINT uq_supermarkets_name UNIQUE (name)
);

-- Partial index: active supermarkets by name (soft-delete aware)
CREATE INDEX idx_supermarkets_name_active ON supermarkets (name) WHERE deleted_at IS NULL;

