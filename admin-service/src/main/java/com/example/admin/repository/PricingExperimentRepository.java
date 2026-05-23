package com.example.admin.repository;

import com.example.admin.entity.PricingExperiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricingExperimentRepository extends JpaRepository<PricingExperiment, Long> {
}
