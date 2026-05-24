CREATE TABLE IF NOT EXISTS telemetry_events (
    device_id String,
    event_type String,
    price_before Float64,
    price_after Float64,
    timestamp DateTime
) ENGINE = MergeTree()
ORDER BY (device_id, timestamp);
