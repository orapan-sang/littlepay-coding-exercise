package com.littlepay.service;

import com.littlepay.model.FareRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FareRuleMatrixTest {
    public static final String VALID_FARE_RULE_CSV = "src/test/resources/rule/valid-fare-rule.csv";
    public static final String INVALID_FARE_RULE_CSV = "src/test/resources/rule/invalid-fare-rule.csv";

    @Test
    void loadValidFareRule() {
        // FromStopId, ToStopId, FareAmount
        // 1,2,3.25
        FareRuleMatrix fareRuleMatrix = new FareRuleMatrix();
        int count = fareRuleMatrix.load(VALID_FARE_RULE_CSV);
        assertEquals(1, count);

        // Expect 2 rules found for stop 1 & 2
        assertEquals(2, fareRuleMatrix.FARE_RULES.size());

        Map<String, BigDecimal> stop1 = new HashMap<>();
        stop1.put("1", new BigDecimal("0"));
        stop1.put("2", new BigDecimal("3.25"));
        assertEquals(fareRuleMatrix.FARE_RULES.get("1"), stop1);

        Map<String, BigDecimal> stop2 = new HashMap<>();
        stop2.put("1", new BigDecimal("3.25"));
        stop2.put("2", new BigDecimal("0"));
        assertEquals(fareRuleMatrix.FARE_RULES.get("2"), stop2);
    }

    @Test
    void loadInvalidFareRule() {
        // FromStopId, ToStopId, FareAmount
        // 1,2,"unknown"
        // 2,3,5.5
        FareRuleMatrix fareRuleMatrix = new FareRuleMatrix();
        int count = fareRuleMatrix.load(INVALID_FARE_RULE_CSV);
        // Skip one invalid row
        assertEquals(1, count);

        // Expect 2 rules found for stop 2 & 3
        assertEquals(2, fareRuleMatrix.FARE_RULES.size());

        Map<String, BigDecimal> stop2 = new HashMap<>();
        stop2.put("2", new BigDecimal("0"));
        stop2.put("3", new BigDecimal("5.5"));
        assertEquals(fareRuleMatrix.FARE_RULES.get("2"), stop2);

        Map<String, BigDecimal> stop3 = new HashMap<>();
        stop3.put("2", new BigDecimal("5.5"));
        stop3.put("3", new BigDecimal("0"));
        assertEquals(fareRuleMatrix.FARE_RULES.get("3"), stop3);
    }

    @Test
    void addFareRules() {
        // ---- 1. Add the first rule ----
        FareRuleMatrix fareRuleMatrix = new FareRuleMatrix();
        FareRule rule1 = new FareRule("1", "2", new BigDecimal("3.25"));
        fareRuleMatrix.add(rule1);
        fareRuleMatrix.FARE_RULES.forEach((key, value) -> System.out.println("DEBUG => "+key + ":" + value));
        // Expect 1 rule found for stop 1
        assertEquals(1, fareRuleMatrix.FARE_RULES.size());

        Map<String, BigDecimal> stop1 = new HashMap<>();
        stop1.put("1", new BigDecimal("0"));
        stop1.put("2", new BigDecimal("3.25"));
        assertEquals(fareRuleMatrix.FARE_RULES.get("1"), stop1);

        // Expect 2 max fare rules found for stop 1 & 2
        assertEquals(1, fareRuleMatrix.MAX_FARE_RULES.size());
        assertEquals(new BigDecimal("3.25"), fareRuleMatrix.MAX_FARE_RULES.get("1"));

        // ---- 2. Add the second rule ----
        FareRule rule2 = new FareRule("2", "3", new BigDecimal("5.5"));
        fareRuleMatrix.add(rule2);

        // Expect 2 rule found for stop 1 & 2
        assertEquals(2, fareRuleMatrix.FARE_RULES.size());

        // Stop 2 will have a new fare to stop 3 added
        Map<String, BigDecimal> stop2 = new HashMap<>();
        stop2.put("2", new BigDecimal("0"));
        stop2.put("3", new BigDecimal("5.5"));
        assertEquals(fareRuleMatrix.FARE_RULES.get("2"), stop2);

        // Expect 3 max fare rules found for stop 1 & 2
        assertEquals(2, fareRuleMatrix.MAX_FARE_RULES.size());
        assertEquals(new BigDecimal("3.25"), fareRuleMatrix.MAX_FARE_RULES.get("1"));
        assertEquals(new BigDecimal("5.5"), fareRuleMatrix.MAX_FARE_RULES.get("2"));

        // ---- 3. Add the third rule ----
        FareRule rule3 = new FareRule("1", "3", new BigDecimal("7.3"));
        fareRuleMatrix.add(rule3);

        // Stop 1 will have a new fare to stop 3 added
        Map<String, BigDecimal> stop1Updated = new HashMap<>();
        stop1Updated.put("1", new BigDecimal("0"));
        stop1Updated.put("2", new BigDecimal("3.25"));
        stop1Updated.put("3", new BigDecimal("7.3"));
        assertEquals(fareRuleMatrix.FARE_RULES.get("1"), stop1Updated);

        // Expect 3 max fare rules found for stop 1 & 2
        assertEquals(2, fareRuleMatrix.MAX_FARE_RULES.size());
        assertEquals(new BigDecimal("7.3"), fareRuleMatrix.MAX_FARE_RULES.get("1"));
        assertEquals(new BigDecimal("5.5"), fareRuleMatrix.MAX_FARE_RULES.get("2"));
    }
}