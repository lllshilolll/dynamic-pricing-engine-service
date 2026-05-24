package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceChangeEvent {
    private String deviceId;
    private double priceBefore;
    private double priceAfter;
    private String eventType;
    private long timestamp;
}
