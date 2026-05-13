package com.example.pricing.service.repository;

import com.example.pricing.dto.PriceUpdate;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingRepository extends ReactiveMongoRepository<PriceUpdate, String> {

//    Flux<PriceUpdate> findAllByDeviceIdOrderByCreatedAtDesc(String deviceId);
}