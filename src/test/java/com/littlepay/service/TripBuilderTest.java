package com.littlepay.service;

import com.littlepay.model.Tap;
import com.littlepay.model.TapType;
import com.littlepay.model.Trip;
import com.littlepay.model.TripStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TripBuilderTest {
    public static final String VALID_TAP_CSV = "src/test/resources/input/valid-tap.csv";
    public static final String INVALID_TAP_CSV = "src/test/resources/input/invalid-tap.csv";

    static FareRuleMatrix fareRuleMatrix = new FareRuleMatrix();

    @BeforeAll
    public static void setUpFareRules() {
        Map<String, BigDecimal> stop1 = new HashMap<>();
        stop1.put("1", new BigDecimal("0"));
        stop1.put("2", new BigDecimal("3.25"));
        stop1.put("3", new BigDecimal("7.3"));
        fareRuleMatrix.FARE_RULES.put("1", stop1);
        fareRuleMatrix.MAX_FARE_RULES.put("1", new BigDecimal("7.3"));

        Map<String, BigDecimal> stop2 = new HashMap<>();
        stop2.put("1", new BigDecimal("3.25"));
        stop2.put("2", new BigDecimal("0"));
        stop2.put("3", new BigDecimal("5.5"));
        fareRuleMatrix.FARE_RULES.put("2", stop2);
        fareRuleMatrix.MAX_FARE_RULES.put("2", new BigDecimal("5.5"));

        Map<String, BigDecimal> stop3 = new HashMap<>();
        stop3.put("1", new BigDecimal("7.3"));
        stop3.put("2", new BigDecimal("5.5"));
        stop3.put("3", new BigDecimal("0"));
        fareRuleMatrix.FARE_RULES.put("3", stop3);
        fareRuleMatrix.MAX_FARE_RULES.put("3", new BigDecimal("7.3"));
    }

    @Test
    void loadValidTapsToTrip() {
        // ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
        // 1, 02-01-2022 10:55:00, ON, 1, CompleteTrip, Bus37, 5500005555555559
        // 2, 02-01-2022 11:00:00, OFF, 3, CompleteTrip, Bus37, 5500005555555559
        TripBuilder tripBuilder = new TripBuilder(fareRuleMatrix);
        List<Trip> tripList = tripBuilder.loadTapsAndProcess(VALID_TAP_CSV);

        // Expect 1 trip
        assertEquals(1, tripList.size());

        Trip actualTrip = tripList.get(0);
        Trip expectedTrip = new Trip();
        expectedTrip.setBusId("Bus37");
        expectedTrip.setCompanyId("CompleteTrip");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("1");
        expectedTrip.setToStopId("3");
        expectedTrip.setStarted(1641120900L);
        expectedTrip.setFinished(1641121200L);
        expectedTrip.setDurationSecs(300L);
        expectedTrip.setChargeAmount(new BigDecimal("7.3"));
        expectedTrip.setStatus(TripStatus.COMPLETE);
        assertEquals(expectedTrip, actualTrip);
    }

    @Test
    void loadInvalidTapsToTrip() {
        // ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
        // 1, 02-01-2022 08:11:00, UNKNOWN, 2, TapOnWithoutTapOff1, Bus20, 3528000700000000
        // 2, 02-01-2022 10:55:00, ON, 1, IncompleteTrip, Bus37, 5500005555555559
        TripBuilder tripBuilder = new TripBuilder(fareRuleMatrix);
        List<Trip> tripList = tripBuilder.loadTapsAndProcess(INVALID_TAP_CSV);
        // Skip one invalid row
        // Expect 1 trip
        assertEquals(1, tripList.size());

        Trip actualTrip = tripList.get(0);
        Trip expectedTrip = new Trip();
        expectedTrip.setBusId("Bus37");
        expectedTrip.setCompanyId("IncompleteTrip");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("1");
        expectedTrip.setStarted(1641120900L);
        expectedTrip.setChargeAmount(new BigDecimal("7.3"));
        expectedTrip.setStatus(TripStatus.INCOMPLETE);
        assertEquals(expectedTrip, actualTrip);
    }

    @Test
    void processCompleteTrip() {
        // ---- TAP ON ----
        Tap tapCompleteOn = new Tap();
        tapCompleteOn.setId(1L);
        tapCompleteOn.setPan("5500005555555559");
        tapCompleteOn.setBusId("Bus1");
        tapCompleteOn.setType(TapType.ON);
        tapCompleteOn.setCompanyId("Complete1");
        tapCompleteOn.setStopId("1");
        tapCompleteOn.setDateTimeInSecs(1641034800L);  // 01-01-2022 11:00:00
        TripBuilder tripBuilder = new TripBuilder(fareRuleMatrix);
        Trip trip = tripBuilder.processTap(tapCompleteOn);
        assertNull(trip);

        // ---- TAP OFF ----
        Tap tapCompleteOff = new Tap();
        tapCompleteOff.setId(2L);
        tapCompleteOff.setPan("5500005555555559");
        tapCompleteOff.setBusId("Bus1");
        tapCompleteOff.setType(TapType.OFF);
        tapCompleteOff.setCompanyId("Complete1");
        tapCompleteOff.setStopId("3");
        tapCompleteOff.setDateTimeInSecs(1641035400L);  // 01-01-2022 11:10:00
        Trip actualTrip = tripBuilder.processTap(tapCompleteOff);

        Trip expectedTrip = new Trip();
        expectedTrip.setBusId("Bus1");
        expectedTrip.setCompanyId("Complete1");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("1");
        expectedTrip.setToStopId("3");
        expectedTrip.setStarted(1641034800L);
        expectedTrip.setFinished(1641035400L);
        expectedTrip.setDurationSecs(600L);
        expectedTrip.setChargeAmount(new BigDecimal("7.3"));
        expectedTrip.setStatus(TripStatus.COMPLETE);
        assertEquals(expectedTrip, actualTrip);
    }

    @Test
    void processInCompleteTrip() {
        // ---- TAP OFF ----
        Tap tapOffOnly = new Tap();
        tapOffOnly.setId(1L);
        tapOffOnly.setPan("5500005555555559");
        tapOffOnly.setBusId("Bus1");
        tapOffOnly.setType(TapType.OFF);
        tapOffOnly.setCompanyId("OnlyTapOff");
        tapOffOnly.setStopId("3");
        tapOffOnly.setDateTimeInSecs(1641035400L);  // 01-01-2022 11:10:00
        TripBuilder tripBuilder = new TripBuilder(fareRuleMatrix);
        Trip trip = tripBuilder.processTap(tapOffOnly);
        assertNull(trip);

        // ---- TAP ON without TAP OFF ----
        Tap incompleteTap1 = new Tap();
        incompleteTap1.setId(2L);
        incompleteTap1.setPan("5500005555555559");
        incompleteTap1.setBusId("Bus1");
        incompleteTap1.setType(TapType.ON);
        incompleteTap1.setCompanyId("Incomplete");
        incompleteTap1.setStopId("1");
        incompleteTap1.setDateTimeInSecs(1641035700L);  // 01-01-2022 11:15:00
        trip = tripBuilder.processTap(incompleteTap1);
        assertNull(trip);

        // ---- TAP ON without TAP OFF (2nd time) ----
        Tap incompleteTap2 = new Tap();
        incompleteTap2.setId(3L);
        incompleteTap2.setPan("5500005555555559");
        incompleteTap2.setBusId("Bus1");
        incompleteTap2.setType(TapType.ON);
        incompleteTap2.setCompanyId("Incomplete");
        incompleteTap2.setStopId("2");
        incompleteTap2.setDateTimeInSecs(1641036000L);  // 01-01-2022 11:20:00
        // Marked 1st incompleteTap as an INCOMPLETE trip
        Trip actualTrip = tripBuilder.processTap(incompleteTap2);

        Trip expectedTrip = new Trip();
        expectedTrip.setBusId("Bus1");
        expectedTrip.setCompanyId("Incomplete");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("1");
        expectedTrip.setStarted(1641035700L);
        expectedTrip.setChargeAmount(new BigDecimal("7.3"));
        expectedTrip.setStatus(TripStatus.INCOMPLETE);
        assertEquals(expectedTrip, actualTrip);

        // ---- TAP ON without TAP OFF (3nd time) by different bus traveller ----
        Tap incompleteTap3 = new Tap();
        incompleteTap3.setId(4L);
        incompleteTap3.setPan("4462030000000000");
        incompleteTap3.setBusId("Bus1");
        incompleteTap3.setType(TapType.ON);
        incompleteTap3.setCompanyId("Incomplete");
        incompleteTap3.setStopId("3");
        incompleteTap3.setDateTimeInSecs(1641036000L);  // 01-01-2022 11:20:00
        // Marked 1st incompleteTap as an INCOMPLETE trip
        trip = tripBuilder.processTap(incompleteTap3);
        assertNull(trip);

        // Get the 2nd and 3rd INCOMPLETE TRIP from BUS_TRAVELLER_TAP_ON
        List<Trip> incompleteTrips = tripBuilder.finalizeIncompleteTrip();
        assertEquals(2, incompleteTrips.size());
        // From incompleteTap2
        expectedTrip = new Trip();
        expectedTrip.setBusId("Bus1");
        expectedTrip.setCompanyId("Incomplete");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("2");
        expectedTrip.setStarted(1641036000L);
        expectedTrip.setChargeAmount(new BigDecimal("5.5"));
        expectedTrip.setStatus(TripStatus.INCOMPLETE);
        assertEquals(expectedTrip, incompleteTrips.get(0));
        // From incompleteTap3
        expectedTrip = new Trip();
        expectedTrip.setBusId("Bus1");
        expectedTrip.setCompanyId("Incomplete");
        expectedTrip.setPan("4462030000000000");
        expectedTrip.setFromStopId("3");
        expectedTrip.setStarted(1641036000L);
        expectedTrip.setChargeAmount(new BigDecimal("7.3"));
        expectedTrip.setStatus(TripStatus.INCOMPLETE);
        assertEquals(expectedTrip, incompleteTrips.get(1));
    }

    @Test
    void processCancelledTrip() {
        // ---- TAP ON ----
        Tap tapCancelledOn = new Tap();
        tapCancelledOn.setId(1L);
        tapCancelledOn.setPan("5500005555555559");
        tapCancelledOn.setBusId("Bus1");
        tapCancelledOn.setType(TapType.ON);
        tapCancelledOn.setCompanyId("Cancelled");
        tapCancelledOn.setStopId("1");
        tapCancelledOn.setDateTimeInSecs(1641034800L);  // 01-01-2022 11:00:00
        TripBuilder tripBuilder = new TripBuilder(fareRuleMatrix);
        Trip trip = tripBuilder.processTap(tapCancelledOn);
        assertNull(trip);

        // ---- TAP OFF ----
        Tap tapCancelledOff = new Tap();
        tapCancelledOff.setId(2L);
        tapCancelledOff.setPan("5500005555555559");
        tapCancelledOff.setBusId("Bus1");
        tapCancelledOff.setType(TapType.OFF);
        tapCancelledOff.setCompanyId("Cancelled");
        tapCancelledOff.setStopId("1");
        tapCancelledOff.setDateTimeInSecs(1641034860L);  // 01-01-2022 11:01:00
        Trip actualTrip = tripBuilder.processTap(tapCancelledOff);

        Trip expectedTrip = new Trip();
        expectedTrip.setBusId("Bus1");
        expectedTrip.setCompanyId("Cancelled");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("1");
        expectedTrip.setToStopId("1");
        expectedTrip.setStarted(1641034800L);
        expectedTrip.setFinished(1641034860L);
        expectedTrip.setDurationSecs(60L);
        expectedTrip.setChargeAmount(new BigDecimal("0"));
        expectedTrip.setStatus(TripStatus.CANCELLED);
        assertEquals(expectedTrip, actualTrip);

        // ---- TAP ON & OFF at the same time ----
        tapCancelledOn = new Tap();
        tapCancelledOn.setId(1L);
        tapCancelledOn.setPan("5500005555555559");
        tapCancelledOn.setBusId("Bus1");
        tapCancelledOn.setType(TapType.ON);
        tapCancelledOn.setCompanyId("Cancelled");
        tapCancelledOn.setStopId("1");
        tapCancelledOn.setDateTimeInSecs(1641034800L);  // 01-01-2022 11:00:00
        trip = tripBuilder.processTap(tapCancelledOn);
        assertNull(trip);

        tapCancelledOff = new Tap();
        tapCancelledOff.setId(2L);
        tapCancelledOff.setPan("5500005555555559");
        tapCancelledOff.setBusId("Bus1");
        tapCancelledOff.setType(TapType.OFF);
        tapCancelledOff.setCompanyId("Cancelled");
        tapCancelledOff.setStopId("1");
        tapCancelledOff.setDateTimeInSecs(1641034800L);  // 01-01-2022 11:01:00
        actualTrip = tripBuilder.processTap(tapCancelledOff);

        expectedTrip = new Trip();
        expectedTrip.setBusId("Bus1");
        expectedTrip.setCompanyId("Cancelled");
        expectedTrip.setPan("5500005555555559");
        expectedTrip.setFromStopId("1");
        expectedTrip.setToStopId("1");
        expectedTrip.setStarted(1641034800L);
        expectedTrip.setFinished(1641034800L);
        expectedTrip.setDurationSecs(0L);
        expectedTrip.setChargeAmount(new BigDecimal("0"));
        expectedTrip.setStatus(TripStatus.CANCELLED);
        assertEquals(expectedTrip, actualTrip);
    }
}