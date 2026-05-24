package com.example.admin.analytics;

import com.example.dto.PriceChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class ClickHouseWriter {

    private final JdbcTemplate clickhouseJdbcTemplate;
    private final ConcurrentLinkedQueue<PriceChangeEvent> buffer = new ConcurrentLinkedQueue<>();

    @Value("${clickhouse.batch-size:100}")
    private int batchSize;

    public ClickHouseWriter(@Qualifier("clickhouseJdbcTemplate") JdbcTemplate clickhouseJdbcTemplate) {
        this.clickhouseJdbcTemplate = clickhouseJdbcTemplate;
    }

    public void recordEvent(PriceChangeEvent event) {
        buffer.add(event);
    }

    @Scheduled(fixedDelayString = "${clickhouse.flush-interval-ms:5000}")
    public void flush() {
        if (buffer.isEmpty()) return;

        List<PriceChangeEvent> batch = new ArrayList<>();
        while (!buffer.isEmpty() && batch.size() < batchSize) {
            PriceChangeEvent record = buffer.poll();
            if (record != null) batch.add(record);
        }
        if (batch.isEmpty()) return;

        try {
            clickhouseJdbcTemplate.batchUpdate(
                    "INSERT INTO telemetry_events (device_id, event_type, price_before, price_after, timestamp) VALUES (?, ?, ?, ?, ?)",
                    batch,
                    batch.size(),
                    (ps, r) -> {
                        ps.setString(1, r.getDeviceId());
                        ps.setString(2, r.getEventType());
                        ps.setDouble(3, r.getPriceBefore());
                        ps.setDouble(4, r.getPriceAfter());
                        ps.setTimestamp(5, new java.sql.Timestamp(r.getTimestamp()));
                    }
            );
            log.info("Записано {} событий в ClickHouse", batch.size());
        } catch (Exception e) {
            log.error("Ошибка записи батча в ClickHouse: {}", e.getMessage());
            buffer.addAll(batch);
        }
    }
}
