package com.example.pricing.controller;

import com.example.pricing.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping(path = "/api/prices", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@CrossOrigin
public class PriceController {

    private final RedisService redisService;

    @GetMapping
    public Mono<List<DevicePrice>> getAllPrices() {
        return redisService.getAllPrices()
                .map(entry -> new DevicePrice(entry.getKey(), Double.parseDouble(entry.getValue())))
                .collectList();
    }

    public record DevicePrice(String deviceId, double price) {}
}
