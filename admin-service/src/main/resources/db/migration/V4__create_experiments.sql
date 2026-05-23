CREATE TABLE pricing_experiments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    rule_a_id BIGINT REFERENCES pricing_rules(id),
    rule_b_id BIGINT REFERENCES pricing_rules(id),
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP
);
