package com.littlepay.service.builder;

import com.littlepay.service.bean.FareRule;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.littlepay.service.ExerciseMain.*;

public class FareRuleBuilder {
    public static void add(FareRule rule) {
        if (rule == null)
            return;

        // Create or update a rule for 'FromStopId'
        Map<String, BigDecimal> toStopFares = new HashMap<>();
        if (FARE_RULES.containsKey(rule.getFromStopId())) {
            toStopFares = objectMapper.convertValue(FARE_RULES.get(rule.getFromStopId()), Map.class);
            toStopFares.put(rule.getToStopId(), rule.getFareAmount());
        }
        else {
            // New stop rule
            toStopFares.put(rule.getFromStopId(), BigDecimal.ZERO);     // Rule for Tap ON & OFF at the same stop
            toStopFares.put(rule.getToStopId(), rule.getFareAmount());
        }
        FARE_RULES.put(rule.getFromStopId(), toStopFares);
        updateMaxFareRule(rule.getFromStopId(), rule.getFareAmount());
    }

    private static void updateMaxFareRule(String fromStopId, BigDecimal fareAmount) {
        if (MAX_FARE_RULES.containsKey(fromStopId)) {
            if (MAX_FARE_RULES.get(fromStopId).compareTo(fareAmount) >= 0)
                return;
        }
        MAX_FARE_RULES.put(fromStopId, fareAmount);
    }
}
