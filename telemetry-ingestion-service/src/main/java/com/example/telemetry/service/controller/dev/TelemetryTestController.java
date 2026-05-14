package com.example.telemetry.service.controller.dev;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.TelemetryService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class TelemetryTestController {

    private final TelemetryService telemetryService;

    @GetMapping("/send")
    public Mono<String> sendTestEvent() {
        TelemetryEvent event = new TelemetryEvent(
                "device-123",
                "HIGH_DEMAND",
                "Hello",
                1L);
        return telemetryService.saveTelemetry(event);
    }
}