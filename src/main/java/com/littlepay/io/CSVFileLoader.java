package com.littlepay.io;

import com.littlepay.log.Log;
import com.littlepay.service.bean.BusTraveller;
import com.littlepay.service.bean.FareRule;
import com.littlepay.service.bean.Tap;
import com.littlepay.service.bean.TapType;
import com.littlepay.service.builder.FareRuleBuilder;
import com.littlepay.service.builder.TripBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.littlepay.service.ExerciseMain.DATETIME_FORMATTER;
import static com.littlepay.service.ExerciseMain.UTC_TIME_ZONE;

public class CSVFileLoader {
    public static String DEFAULT_FARE_RULE_PATH = "src/main/resources/rule/fare-rule.csv";
    public static String DEFAULT_TAP_PATH = "src/main/resources/input/taps.csv";
    public static String DEFAULT_TRIP_PATH = "src/main/resources/output/trips.csv";

    public static int loadFareRules(String fileName) {
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
                    FareRuleBuilder.add(rule);

                    // Create a rule: ToStopId - FromStopId
                    FareRule reverseRule = new FareRule(rule.getToStopId(), rule.getFromStopId(), rule.getFareAmount());
                    // Add to FARE_RULES
                    FareRuleBuilder.add(reverseRule);

                    count++;
                }
                catch (Exception e) {
                    Log.warning("Cannot read fare rule: {0}", new Object[]{row}, e);
                }
            }
        }
        catch (Exception e) {
            Log.warning("Cannot read {0}", new Object[]{fileName}, e);
        }
        return count;
    }

    public static int loadTapsToTrip(String fileName) {
        int count = 0;
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(fileName)).withSkipLines(1).build()) {
            String[] row;
            // Read tap line by line
            // Format: ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
            while ((row = csvReader.readNext()) != null) {
                // Skip invalid line
                if (row.length != 7)
                    continue;

                try {
                    LocalDateTime dateTime = LocalDateTime.parse(row[1].trim(), DATETIME_FORMATTER);

                    Tap tap = new Tap();
                    tap.setId(Long.parseLong(row[0].trim()));
                    tap.setDateTimeInSecs(dateTime.toEpochSecond(UTC_TIME_ZONE));
                    tap.setType(TapType.valueOf(row[2].trim()));
                    tap.setStopId(row[3].trim());
                    tap.setCompanyId(row[4].trim());
                    tap.setBusId(row[5].trim());
                    tap.setPan(row[6].trim());

                    LocalDate date = dateTime.toLocalDate();
                    BusTraveller traveller = new BusTraveller(tap.getCompanyId(), tap.getBusId(), tap.getPan(),
                                                              date.atStartOfDay(UTC_TIME_ZONE).toEpochSecond());

                    TripBuilder.processTravellerTap(traveller, tap);

                    count++;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.warning("Cannot read tap: {0}", new Object[]{Arrays.toString(row)}, e);
                }
            }
            TripBuilder.finalizeIncompleteTrip();
        }
        catch (Exception e) {
            Log.warning("Cannot read {0}", new Object[]{fileName}, e);
        }
        return count;
    }
}
