package com.example.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "price_updates")
public class PriceUpdate {
    private @Id String id;        // Уникальный ID записи
    private String deviceId;         // ID самоката/курьера
    private double priceCoefficient; // Результат вычислений
    private LocalDateTime createdAt; // Временная метка изменения
}