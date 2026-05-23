package com.example.pricing.service.clickhouse;

import com.example.dto.TelemetryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class ClickHouseWriter {

    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentLinkedQueue<TelemetryRecord> buffer = new ConcurrentLinkedQueue<>();

    @Value("${clickhouse.batch-size:100}")
    private int batchSize;

    public ClickHouseWriter(@Qualifier("clickhouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Mono<Void> recordEvent(TelemetryEvent event, double priceBefore, double priceAfter) {
        return Mono.<Void>create((MonoSink<Void> sink) -> {
            buffer.add(new TelemetryRecord(
                    event.getDeviceId(),
                    event.getEventType(),
                    event.getPayload(),
                    priceBefore,
                    priceAfter,
                    new java.util.Date()
            ));
            sink.success();
        });
    }

    @Scheduled(fixedDelayString = "${clickhouse.flush-interval-ms:5000}")
    public void flush() {
        if (buffer.isEmpty()) return;

        List<TelemetryRecord> batch = new ArrayList<>();
        while (!buffer.isEmpty() && batch.size() < batchSize) {
            TelemetryRecord record = buffer.poll();
            if (record != null) batch.add(record);
        }
        if (batch.isEmpty()) return;

        try {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO telemetry_events (device_id, event_type, payload, price_before, price_after, timestamp) VALUES (?, ?, ?, ?, ?, ?)",
                    batch,
                    batch.size(),
                    (ps, r) -> {
                        ps.setString(1, r.deviceId());
                        ps.setString(2, r.eventType());
                        ps.setString(3, r.payload());
                        ps.setDouble(4, r.priceBefore());
                        ps.setDouble(5, r.priceAfter());
                        ps.setTimestamp(6, new java.sql.Timestamp(r.timestamp().getTime()));
                    }
            );
            log.info("Записано {} событий в ClickHouse", batch.size());
        } catch (Exception e) {
            log.error("Ошибка записи батча в ClickHouse: {}", e.getMessage());
            buffer.addAll(batch);
        }
    }

    private record TelemetryRecord(
            String deviceId, String eventType, String payload,
            double priceBefore, double priceAfter, java.util.Date timestamp
    ) {}
}