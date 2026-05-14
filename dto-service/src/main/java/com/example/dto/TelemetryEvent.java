package com.example.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryEvent {
    private String deviceId;
    private String eventType; // e.g., "LOCATION_UPDATE", "ORDER_PLACED"
    private String payload;
    private long timestamp;
}