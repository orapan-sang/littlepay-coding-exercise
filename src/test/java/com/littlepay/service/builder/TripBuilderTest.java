package com.littlepay.service.builder;

import com.littlepay.service.bean.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.littlepay.service.ExerciseMain.*;
import static com.littlepay.service.builder.TripBuilder.BUS_TRAVELLER_TAP_ON;
import static org.junit.jupiter.api.Assertions.*;

class TripBuilderTest {
    @BeforeAll
    public static void setUpFareRules() {
        Map<String, BigDecimal> stop1 = new HashMap<>();
        stop1.put("1", new BigDecimal("0"));
        stop1.put("2", new BigDecimal("3.25"));
        stop1.put("3", new BigDecimal("7.3"));
        FARE_RULES.put("1", stop1);
        MAX_FARE_RULES.put("1", new BigDecimal("7.3"));

        Map<String, BigDecimal> stop2 = new HashMap<>();
        stop2.put("1", new BigDecimal("3.25"));
        stop2.put("2", new BigDecimal("0"));
        stop2.put("3", new BigDecimal("5.5"));
        FARE_RULES.put("2", stop2);
        MAX_FARE_RULES.put("2", new BigDecimal("5.5"));

        Map<String, BigDecimal> stop3 = new HashMap<>();
        stop3.put("1", new BigDecimal("7.3"));
        stop3.put("2", new BigDecimal("5.5"));
        stop3.put("3", new BigDecimal("0"));
        FARE_RULES.put("3", stop3);
        MAX_FARE_RULES.put("3", new BigDecimal("7.3"));
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
        Trip trip = TripBuilder.processTap(tapCompleteOn);
        assertNull(trip);
        // tapCompleteOn will be added to BUS_TRAVELLER_TAP_ON
        assertEquals(tapCompleteOn, BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(tapCompleteOn)));

        // ---- TAP OFF ----
        Tap tapCompleteOff = new Tap();
        tapCompleteOff.setId(2L);
        tapCompleteOff.setPan("5500005555555559");
        tapCompleteOff.setBusId("Bus1");
        tapCompleteOff.setType(TapType.OFF);
        tapCompleteOff.setCompanyId("Complete1");
        tapCompleteOff.setStopId("3");
        tapCompleteOff.setDateTimeInSecs(1641035400L);  // 01-01-2022 11:10:00
        Trip actualTrip = TripBuilder.processTap(tapCompleteOff);
        // Complete a trip so Tap ON for this bus traveller will be removed
        assertNull(BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(tapCompleteOff)));

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
        Trip trip = TripBuilder.processTap(tapOffOnly);
        assertNull(trip);
        // Tap off won't be stored in BUS_TRAVELLER_TAP_ON
        assertNull(BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(tapOffOnly)));

        // ---- TAP ON without TAP OFF ----
        Tap incompleteTap1 = new Tap();
        incompleteTap1.setId(2L);
        incompleteTap1.setPan("5500005555555559");
        incompleteTap1.setBusId("Bus1");
        incompleteTap1.setType(TapType.ON);
        incompleteTap1.setCompanyId("Incomplete");
        incompleteTap1.setStopId("1");
        incompleteTap1.setDateTimeInSecs(1641035700L);  // 01-01-2022 11:15:00
        trip = TripBuilder.processTap(incompleteTap1);
        assertNull(trip);
        // incompleteTap1 will be added to BUS_TRAVELLER_TAP_ON
        assertEquals(incompleteTap1, BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(incompleteTap1)));

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
        Trip actualTrip = TripBuilder.processTap(incompleteTap2);
        // incompleteTap2 will be added to BUS_TRAVELLER_TAP_ON
        assertEquals(incompleteTap2, BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(incompleteTap2)));

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
        trip = TripBuilder.processTap(incompleteTap3);
        assertNull(trip);
        // incompleteTap3 will be added to BUS_TRAVELLER_TAP_ON
        assertEquals(incompleteTap3, BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(incompleteTap3)));

        // Get the 2nd and 3rd INCOMPLETE TRIP from BUS_TRAVELLER_TAP_ON
        List<Trip> incompleteTrips = TripBuilder.finalizeIncompleteTrip();
        // Marked all trips left in BUS_TRAVELLER_TAP_ON as INCOMPLETE
        assertNull(BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(tapOffOnly)));
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
        Trip trip = TripBuilder.processTap(tapCancelledOn);
        assertNull(trip);
        // tapCompleteOn will be added to BUS_TRAVELLER_TAP_ON
        assertEquals(tapCancelledOn, BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(tapCancelledOn)));

        // ---- TAP OFF ----
        Tap tapCancelledOff = new Tap();
        tapCancelledOff.setId(2L);
        tapCancelledOff.setPan("5500005555555559");
        tapCancelledOff.setBusId("Bus1");
        tapCancelledOff.setType(TapType.OFF);
        tapCancelledOff.setCompanyId("Cancelled");
        tapCancelledOff.setStopId("1");
        tapCancelledOff.setDateTimeInSecs(1641034860L);  // 01-01-2022 11:01:00
        Trip actualTrip = TripBuilder.processTap(tapCancelledOff);
        // Complete a trip so Tap ON for this bus traveller will be removed
        assertNull(BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(tapCancelledOff)));

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
        trip = TripBuilder.processTap(tapCancelledOn);
        assertNull(trip);
        // tapCompleteOn will be added to BUS_TRAVELLER_TAP_ON
        assertEquals(tapCancelledOn, BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(tapCancelledOn)));

        tapCancelledOff = new Tap();
        tapCancelledOff.setId(2L);
        tapCancelledOff.setPan("5500005555555559");
        tapCancelledOff.setBusId("Bus1");
        tapCancelledOff.setType(TapType.OFF);
        tapCancelledOff.setCompanyId("Cancelled");
        tapCancelledOff.setStopId("1");
        tapCancelledOff.setDateTimeInSecs(1641034800L);  // 01-01-2022 11:01:00
        actualTrip = TripBuilder.processTap(tapCancelledOff);
        // Complete a trip so Tap ON for this bus traveller will be removed
        assertNull(BUS_TRAVELLER_TAP_ON.get(TripBuilder.getBusTraveller(tapCancelledOff)));

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

//    @Test
//    void finalizeIncompleteTrip() {
//    }
}