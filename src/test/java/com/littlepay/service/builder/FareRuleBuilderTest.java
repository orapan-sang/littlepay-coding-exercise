package com.littlepay.service.builder;

import com.littlepay.service.bean.FareRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.littlepay.service.ExerciseMain.*;
import static org.junit.jupiter.api.Assertions.*;

class FareRuleBuilderTest {

    @AfterEach
    void cleanUp() {
        FARE_RULES.clear();
        MAX_FARE_RULES.clear();
    }

    @Test
    void addFareRules() {
        // ---- 1. Add the first rule ----
        FareRule rule1 = new FareRule("1", "2", new BigDecimal("3.25"));
        FareRuleBuilder.add(rule1);
        FARE_RULES.forEach((key, value) -> System.out.println("DEBUG => "+key + ":" + value));
        // Expect 1 rule found for stop 1
        assertEquals(1, FARE_RULES.size());

        Map<String, BigDecimal> stop1 = new HashMap<>();
        stop1.put("1", new BigDecimal("0"));
        stop1.put("2", new BigDecimal("3.25"));
        assertTrue(FARE_RULES.get("1").equals(stop1));

        // Expect 2 max fare rules found for stop 1 & 2
        assertEquals(1, MAX_FARE_RULES.size());
        assertEquals(new BigDecimal("3.25"), MAX_FARE_RULES.get("1"));

        // ---- 2. Add the second rule ----
        FareRule rule2 = new FareRule("2", "3", new BigDecimal("5.5"));
        FareRuleBuilder.add(rule2);

        // Expect 2 rule found for stop 1 & 2
        assertEquals(2, FARE_RULES.size());

        // Stop 2 will have a new fare to stop 3 added
        Map<String, BigDecimal> stop2 = new HashMap<>();
        stop2.put("2", new BigDecimal("0"));
        stop2.put("3", new BigDecimal("5.5"));
        assertTrue(FARE_RULES.get("2").equals(stop2));

        // Expect 3 max fare rules found for stop 1 & 2
        assertEquals(2, MAX_FARE_RULES.size());
        assertEquals(new BigDecimal("3.25"), MAX_FARE_RULES.get("1"));
        assertEquals(new BigDecimal("5.5"), MAX_FARE_RULES.get("2"));

        // ---- 3. Add the third rule ----
        FareRule rule3 = new FareRule("1", "3", new BigDecimal("7.3"));
        FareRuleBuilder.add(rule3);

        // Stop 1 will have a new fare to stop 3 added
        Map<String, BigDecimal> stop1Updated = new HashMap<>();
        stop1Updated.put("1", new BigDecimal("0"));
        stop1Updated.put("2", new BigDecimal("3.25"));
        stop1Updated.put("3", new BigDecimal("7.3"));
        assertTrue(FARE_RULES.get("1").equals(stop1Updated));

        // Expect 3 max fare rules found for stop 1 & 2
        assertEquals(2, MAX_FARE_RULES.size());
        assertEquals(new BigDecimal("7.3"), MAX_FARE_RULES.get("1"));
        assertEquals(new BigDecimal("5.5"), MAX_FARE_RULES.get("2"));
    }
}