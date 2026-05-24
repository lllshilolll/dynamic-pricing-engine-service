package com.example.admin.analytics;

import com.example.dto.PriceChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceChangeConsumer {

    private final ClickHouseWriter clickHouseWriter;

    @KafkaListener(topics = "${kafka.topic.price-changes}", groupId = "analytics-group")
    public void consume(PriceChangeEvent event) {
        log.info("Получено событие изменения цены: deviceId={}, {} → {}",
                event.getDeviceId(), event.getPriceBefore(), event.getPriceAfter());
        clickHouseWriter.recordEvent(event);
    }
}
