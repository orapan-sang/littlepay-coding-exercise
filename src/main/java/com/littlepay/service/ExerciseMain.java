package com.littlepay.service;

import com.littlepay.log.Log;
import com.littlepay.model.Trip;

import java.util.*;


public class ExerciseMain {
    public static String DEFAULT_FARE_RULE_PATH = "src/main/resources/rule/fare-rule.csv";
    public static String DEFAULT_TAP_PATH = "src/main/resources/input/taps.csv";
    public static String DEFAULT_TRIP_PATH = "src/main/resources/output/trips.csv";

    public static void main(String[] args) {
        Log.info("Loading fare rules...", null);
        String filename = getEnv("FARE_RULE_PATH", DEFAULT_FARE_RULE_PATH);
        FareRuleMatrix fareRuleMatrix = new FareRuleMatrix();
        int count = fareRuleMatrix.load(filename);
        Log.info("Loaded {0} fare rules.", new Object[]{count});

        Log.info("Loading taps...", null);
        filename = getEnv("TAP_PATH", DEFAULT_TAP_PATH);
        TripBuilder tripBuilder = new TripBuilder(fareRuleMatrix);
        List<Trip> trips = tripBuilder.loadTapsAndProcess(filename);

        Log.info("Writing trips...", null);
        filename = getEnv("TRIP_PATH", DEFAULT_TRIP_PATH);
        boolean success = tripBuilder.exportTripsToCsv(filename, trips);
        Log.info("{0} writing trips to {1}", new Object[]{ (success? "Successfully": "Unsuccessfully"), filename});
    }

    private static String getEnv(String name, String defaultValue) {
        String value = null;
        try {
            value = System.getenv(name);
        }
        catch (Exception e) {
            Log.warning("Cannot get value of environment variable {0}", new Object[]{name}, e);
        }
        return value == null? defaultValue: value;
    }
}
