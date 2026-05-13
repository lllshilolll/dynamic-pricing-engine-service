package com.example.pricing.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "price_updates")
public record PriceUpdate(
        @Id String id,           // Уникальный ID записи
        String deviceId,         // ID самоката/курьера
        double priceCoefficient, // Результат вычислений
        LocalDateTime createdAt  // Временная метка изменения
) {}