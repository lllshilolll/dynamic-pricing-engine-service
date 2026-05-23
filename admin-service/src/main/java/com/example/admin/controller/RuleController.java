package com.example.admin.controller;

import com.example.admin.entity.PricingRule;
import com.example.admin.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/rules", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PricingRule create(@RequestBody PricingRule rule) {
        return ruleService.create(rule);
    }

    @GetMapping
    public List<PricingRule> findAll() {
        return ruleService.findAll();
    }

    @GetMapping("/active")
    public List<PricingRule> findActive() {
        return ruleService.findActive();
    }

    @PutMapping("/{id}")
    public PricingRule update(@PathVariable Long id, @RequestBody PricingRule rule) {
        return ruleService.update(id, rule);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ruleService.delete(id);
    }
}
