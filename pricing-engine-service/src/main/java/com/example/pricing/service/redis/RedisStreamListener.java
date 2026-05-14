package com.example.pricing.service.redis;

import com.example.dto.TelemetryEvent;
import com.example.pricing.service.PricingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class RedisStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    @Autowired
    private ObjectMapper objectMapper;
    private final PricingService pricingService;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> body = message.getValue();
        TelemetryEvent event = objectMapper.convertValue(body, TelemetryEvent.class);
        String messageId = message.getId().getValue();

        System.out.println("Message body: " + body);
        System.out.println("event: " + event);
        System.out.println("ID: " + messageId);


        // Здесь логика обработки
        pricingService.calculateNewPrice(messageId, event);
    }
}