CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL UNIQUE,
    model VARCHAR(255),
    location_id BIGINT REFERENCES locations(id),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
