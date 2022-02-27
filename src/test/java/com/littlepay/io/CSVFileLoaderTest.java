package com.littlepay.io;

import com.littlepay.service.bean.Trip;
import com.littlepay.service.bean.TripStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.littlepay.service.ExerciseMain.*;
import static org.junit.jupiter.api.Assertions.*;

class CSVFileLoaderTest {
    public static final String VALID_FARE_RULE_CSV = "src/test/resources/rule/valid-fare-rule.csv";
    public static final String INVALID_FARE_RULE_CSV = "src/test/resources/rule/invalid-fare-rule.csv";
    public static final String VALID_TAP_CSV = "src/test/resources/input/valid-tap.csv";
    public static final String INVALID_TAP_CSV = "src/test/resources/input/invalid-tap.csv";

    @AfterEach
    void cleanUp() {
        FARE_RULES.clear();
        MAX_FARE_RULES.clear();
        TRIPS.clear();
    }

    @Test
    void loadValidFareRule() {
        // FromStopId, ToStopId, FareAmount
        // 1,2,3.25
        int count = CSVFileLoader.loadFareRules(VALID_FARE_RULE_CSV);
        assertEquals(1, count);

        // Expect 2 rules found for stop 1 & 2
        assertEquals(2, FARE_RULES.size());

        Map<String, BigDecimal> stop1 = new HashMap<>();
        stop1.put("1", new BigDecimal("0"));
        stop1.put("2", new BigDecimal("3.25"));
        assertTrue(FARE_RULES.get("1").equals(stop1));

        Map<String, BigDecimal> stop2 = new HashMap<>();
        stop2.put("1", new BigDecimal("3.25"));
        stop2.put("2", new BigDecimal("0"));
        assertTrue(FARE_RULES.get("2").equals(stop2));
    }

    @Test
    void loadInvalidFareRule() {
        // FromStopId, ToStopId, FareAmount
        // 1,2,"unknown"
        // 2,3,5.5
        int count = CSVFileLoader.loadFareRules(INVALID_FARE_RULE_CSV);
        // Skip one invalid row
        assertEquals(1, count);

        // Expect 2 rules found for stop 2 & 3
        assertEquals(2, FARE_RULES.size());

        Map<String, BigDecimal> stop2 = new HashMap<>();
        stop2.put("2", new BigDecimal("0"));
        stop2.put("3", new BigDecimal("5.5"));
        assertTrue(FARE_RULES.get("2").equals(stop2));

        Map<String, BigDecimal> stop3 = new HashMap<>();
        stop3.put("2", new BigDecimal("5.5"));
        stop3.put("3", new BigDecimal("0"));
        assertTrue(FARE_RULES.get("3").equals(stop3));
    }

    @Test
    void loadValidTapsToTrip() {
        // Set fare rule from Stop 1 to Stop 3
        Map<String, BigDecimal> stop3 = new HashMap<>();
        stop3.put("1", new BigDecimal("0"));
        stop3.put("3", new BigDecimal("5.5"));
        FARE_RULES.put("1", stop3);
        MAX_FARE_RULES.put("1", new BigDecimal("5.5"));

        // ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
        // 1, 02-01-2022 10:55:00, ON, 1, CompleteTrip, Bus37, 5500005555555559
        // 2, 02-01-2022 11:00:00, OFF, 3, CompleteTrip, Bus37, 5500005555555559
        int count = CSVFileLoader.loadTapsToTrip(VALID_TAP_CSV);
        assertEquals(2, count);

        // Expect 1 trip
        assertEquals(1, TRIPS.size());

        Trip actualTrip = TRIPS.get(0);
        Trip expectedTrip = new Trip();
        expectedTrip.setBusId("Bus37");
        expectedTrip.setCompanyId("CompleteTrip");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("1");
        expectedTrip.setToStopId("3");
        expectedTrip.setStarted(1641120900L);
        expectedTrip.setFinished(1641121200L);
        expectedTrip.setDurationSecs(300L);
        expectedTrip.setChargeAmount(new BigDecimal("5.5"));
        expectedTrip.setStatus(TripStatus.COMPLETE);
        assertEquals(expectedTrip, actualTrip);
    }

    @Test
    void loadInvalidTapsToTrip() {
        // Set fare rule from Stop 1 to Stop 3
        Map<String, BigDecimal> stop3 = new HashMap<>();
        stop3.put("1", new BigDecimal("0"));
        stop3.put("3", new BigDecimal("5.5"));
        FARE_RULES.put("1", stop3);
        MAX_FARE_RULES.put("1", new BigDecimal("5.5"));

        // ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
        // 1, 02-01-2022 08:11:00, UNKNOWN, 2, TapOnWithoutTapOff1, Bus20, 3528000700000000
        // 2, 02-01-2022 10:55:00, ON, 1, IncompleteTrip, Bus37, 5500005555555559
        int count = CSVFileLoader.loadTapsToTrip(INVALID_TAP_CSV);
        // Skip one invalid row
        assertEquals(1, count);

        // Expect 1 trip
        assertEquals(1, TRIPS.size());

        Trip actualTrip = TRIPS.get(0);
        Trip expectedTrip = new Trip();
        expectedTrip.setBusId("Bus37");
        expectedTrip.setCompanyId("IncompleteTrip");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("1");
        expectedTrip.setStarted(1641120900L);
        expectedTrip.setChargeAmount(new BigDecimal("5.5"));
        expectedTrip.setStatus(TripStatus.INCOMPLETE);
        assertEquals(expectedTrip, actualTrip);
    }
}