package com.littlepay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlepay.model.FareRule;
import com.littlepay.log.Log;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FareRuleMatrix {
    public ObjectMapper objectMapper = new ObjectMapper();
    // Map<FromStopId, Map<ToStopId, FareAmount>>
    // NOTE: FromStopId and ToStopId are index keys
    protected Map<String, Map<String, BigDecimal>> FARE_RULES  = new HashMap<>();
    // List of possible maximum fare amount from each stop
    protected Map<String, BigDecimal> MAX_FARE_RULES = new HashMap<>();

    public int load(String fileName) {
        int count = 0;
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(fileName)).withSkipLines(1).build()) {
            String[] row;
            // Read rule line by line
            // Format: FromStopId, ToStopId, FareAmount
            while ((row = csvReader.readNext()) != null) {
                try {
                    // Create a rule: FromStopId - ToStopId
                    FareRule rule = new FareRule(row[0].trim(), row[1].trim(), new BigDecimal(row[2].trim()));
                    // Add to FARE_RULES
                    add(rule);

                    // Create a rule: ToStopId - FromStopId
                    FareRule reverseRule = new FareRule(rule.getToStopId(), rule.getFromStopId(), rule.getFareAmount());
                    // Add to FARE_RULES
                    add(reverseRule);

                    count++;
                }
                catch (Exception e) {
                    Log.warning("Cannot read fare rule: {0}", new Object[]{Arrays.toString(row)}, e);
                }
            }
        }
        catch (Exception e) {
            Log.warning("Cannot read {0}", new Object[]{fileName}, e);
        }
        return count;
    }

    public BigDecimal getFare(String fromStopId, String toStopId)
    {
        if (toStopId == null) {
            return MAX_FARE_RULES.get(fromStopId);
        }
        else {
            Map<String, BigDecimal>  toStopFares = objectMapper.convertValue(FARE_RULES.get(fromStopId), Map.class);
            return toStopFares.get(toStopId);
        }
    }

    public void add(FareRule rule) {
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

    private void updateMaxFareRule(String fromStopId, BigDecimal fareAmount) {
        if (MAX_FARE_RULES.containsKey(fromStopId)) {
            if (MAX_FARE_RULES.get(fromStopId).compareTo(fareAmount) >= 0)
                return;
        }
        MAX_FARE_RULES.put(fromStopId, fareAmount);
    }
}
