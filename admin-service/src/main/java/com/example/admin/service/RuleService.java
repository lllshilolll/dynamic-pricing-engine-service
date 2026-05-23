package com.example.admin.service;

import com.example.admin.entity.PricingRule;
import com.example.admin.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleService {

    private final PricingRuleRepository ruleRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public PricingRule create(PricingRule rule) {
        PricingRule saved = ruleRepository.save(rule);
        publishRulesUpdate();
        return saved;
    }

    public List<PricingRule> findAll() {
        return ruleRepository.findAll();
    }

    public List<PricingRule> findActive() {
        return ruleRepository.findAllByActiveTrueOrderByPriorityDesc();
    }

    @Transactional
    public PricingRule update(Long id, PricingRule updated) {
        PricingRule rule = ruleRepository.findById(id).orElseThrow();
        rule.setName(updated.getName());
        rule.setConditionType(updated.getConditionType());
        rule.setConditionValue(updated.getConditionValue());
        rule.setCoefficient(updated.getCoefficient());
        rule.setPriority(updated.getPriority());
        rule.setActive(updated.getActive());
        PricingRule saved = ruleRepository.save(rule);
        publishRulesUpdate();
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        PricingRule rule = ruleRepository.findById(id).orElseThrow();
        rule.setActive(false);
        ruleRepository.save(rule);
        publishRulesUpdate();
    }

    private void publishRulesUpdate() {
        try {
            redisTemplate.convertAndSend("rules:updated", "refresh");
            log.info("Опубликовано событие rules:updated в Redis Pub/Sub");
        } catch (Exception e) {
            log.error("Ошибка публикации rules:updated: {}", e.getMessage());
        }
    }
}
