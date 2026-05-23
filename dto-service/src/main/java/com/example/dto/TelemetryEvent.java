package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryEvent {
    private String deviceId;
    private String eventType;
    private String payload;
    private long timestamp;
}