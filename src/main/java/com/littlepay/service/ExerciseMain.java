package com.littlepay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlepay.io.CSVFileLoader;
import com.littlepay.io.CSVFileWriter;
import com.littlepay.log.Log;
import com.littlepay.service.bean.Trip;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.littlepay.io.CSVFileLoader.*;

public class ExerciseMain {
    public static ObjectMapper objectMapper = new ObjectMapper();
    public static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static ZoneOffset UTC_TIME_ZONE = ZoneOffset.UTC;
    // Map<FromStopId, Map<ToStopId, FareAmount>>
    // NOTE: FromStopId and ToStopId are index keys
    public static Map<String, Map<String, BigDecimal>> FARE_RULES  = new HashMap<>();
    // List of possible maximum fare amount from each stop
    public static Map<String, BigDecimal> MAX_FARE_RULES = new HashMap<>();
    // Trip list, which will be exported to CSV as an output file
    public static List<Trip> TRIPS = new ArrayList<>();

    public static void main(String[] args) {
        Log.info("Loading fare rules...", null);
        String filename = getEnv("FARE_RULE_PATH", DEFAULT_FARE_RULE_PATH);
        int count = CSVFileLoader.loadFareRules(filename);
        Log.info("Loaded {0} fare rules.", new Object[]{count});

        FARE_RULES.forEach((key, value) -> System.out.println("DEBUG => "+key + ":" + value));

        Log.info("Loading taps...", null);
        filename = getEnv("TAP_PATH", DEFAULT_TAP_PATH);
        count = CSVFileLoader.loadTapsToTrip(filename);
        Log.info("Loaded {0} taps.", new Object[]{count});

        Log.info("Writing trips...", null);
        filename = getEnv("TRIP_PATH", DEFAULT_TRIP_PATH);
        boolean success = CSVFileWriter.writeTrips(filename);
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
