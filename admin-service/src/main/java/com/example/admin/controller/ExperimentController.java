package com.example.admin.controller;

import com.example.admin.entity.PricingExperiment;
import com.example.admin.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/experiments", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ExperimentController {

    private final ExperimentService experimentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PricingExperiment create(@RequestBody PricingExperiment experiment) {
        return experimentService.create(experiment);
    }

    @GetMapping
    public List<PricingExperiment> findAll() {
        return experimentService.findAll();
    }

    @PostMapping("/{id}/stop")
    public PricingExperiment stop(@PathVariable Long id) {
        return experimentService.stop(id);
    }
}
