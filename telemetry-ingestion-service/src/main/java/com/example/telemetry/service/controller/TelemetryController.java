package com.example.telemetry.service.controller;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.TelemetryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/telemetry", produces = MediaType.APPLICATION_JSON_VALUE)
public class TelemetryController {

    private final TelemetryService telemetryService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<String> process(@RequestBody TelemetryEvent event) {
        return telemetryService.saveTelemetry(event);
    }
}