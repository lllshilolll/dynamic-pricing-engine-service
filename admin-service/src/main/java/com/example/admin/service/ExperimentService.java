package com.example.admin.service;

import com.example.admin.entity.PricingExperiment;
import com.example.admin.repository.PricingExperimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperimentService {

    private final PricingExperimentRepository repository;

    @Transactional
    public PricingExperiment create(PricingExperiment experiment) {
        return repository.save(experiment);
    }

    public List<PricingExperiment> findAll() {
        return repository.findAll();
    }

    @Transactional
    public PricingExperiment stop(Long id) {
        PricingExperiment experiment = repository.findById(id).orElseThrow();
        experiment.setEndedAt(java.time.LocalDateTime.now());
        return repository.save(experiment);
    }
}
