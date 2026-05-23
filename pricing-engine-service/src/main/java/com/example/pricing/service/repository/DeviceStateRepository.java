package com.example.pricing.service.repository;

import com.example.pricing.dto.DeviceState;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DeviceStateRepository extends ReactiveMongoRepository<DeviceState, String> {
    Mono<DeviceState> findByDeviceId(String deviceId);
}
