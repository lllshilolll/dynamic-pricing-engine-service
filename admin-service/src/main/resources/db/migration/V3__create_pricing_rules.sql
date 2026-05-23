CREATE TABLE pricing_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    condition_type VARCHAR(100) NOT NULL,
    condition_value VARCHAR(255) NOT NULL,
    coefficient DOUBLE PRECISION NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
