package com.littlepay.service.builder;

import com.littlepay.log.Log;
import com.littlepay.service.bean.BusTraveller;
import com.littlepay.service.bean.Tap;
import com.littlepay.service.bean.Trip;
import com.littlepay.service.bean.TripStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.littlepay.service.ExerciseMain.*;

public class TripBuilder {
    // Map of bus traveller who used the same card on the same bus in the same day and Tap ON
    public static Map<BusTraveller, Tap> BUS_TRAVELLER_TAP_ON = new HashMap<>();

    public static Trip processTap(Tap newTap) {
        Trip trip = null;
        BusTraveller traveller = getBusTraveller(newTap);
        if (BUS_TRAVELLER_TAP_ON.containsKey(traveller)) {
            Tap tapOn = BUS_TRAVELLER_TAP_ON.get(traveller);
            trip = matchTapsToTrip(tapOn, newTap);
            if (trip != null) {
                if (trip.getStatus().equals(TripStatus.INCOMPLETE))
                    // Found an INCOMPLETE trip so add a new tap ON to find a matching Tap OFF
                    BUS_TRAVELLER_TAP_ON.put(traveller, newTap);
                else
                    // Remove this Tap ON when newTap is a matching Tap OFF (COMPLETE, CANCELLED)
                    BUS_TRAVELLER_TAP_ON.remove(traveller);
            }
            else {
                Log.fine("Cannot find matching tap for {0}", new Object[]{newTap});
            }
        }
        else {
            if (newTap.getType().isTapOn()) {
                BUS_TRAVELLER_TAP_ON.put(traveller, newTap);
            }
            else {
                Log.fine("Tap OFF without Tap ON: {0}", new Object[]{newTap});
            }
        }
        return trip;
    }

    public static void finalizeIncompleteTrip() {
        // Read all incomplete taps left in BUS_TRAVELLER_TAP_ON
        BUS_TRAVELLER_TAP_ON.forEach((key, value) -> System.out.println("ORAPAN => "+key + ":" + value));
        for (Map.Entry<BusTraveller, Tap> entry : BUS_TRAVELLER_TAP_ON.entrySet()) {
            Trip trip = createTrip(entry.getValue(), null, TripStatus.INCOMPLETE);
            TRIPS.add(trip);
        }
    }

    public static Trip matchTapsToTrip(Tap tapOn, Tap newTap) {
        System.out.println("ON ==> "+tapOn);
        System.out.println("NEW ==> "+newTap);
        // Check if both taps are at the same stop
        if (tapOn.getStopId().equals(newTap.getStopId())) {
            if (validateTapsAtSameStop(tapOn, newTap)) {
                return createTrip(tapOn, newTap, TripStatus.CANCELLED);
            }
            else if (tapOn.getType().isTapOn() && newTap.getType().isTapOn()) {
                return createTrip(tapOn, null, TripStatus.INCOMPLETE);
            }
        }
        else {
            if (validateTapOnAndOff(tapOn, newTap)) {
                return createTrip(tapOn, newTap, TripStatus.COMPLETE);
            }
            else if (tapOn.getType().isTapOn() && newTap.getType().isTapOn()) {
                return createTrip(tapOn, null, TripStatus.INCOMPLETE);
            }
        }
        return null;
    }

    public static BusTraveller getBusTraveller(Tap tap) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(tap.getDateTimeInSecs()), UTC_TIME_ZONE);
        LocalDate date = dateTime.toLocalDate();
        BusTraveller traveller = new BusTraveller(tap.getCompanyId(), tap.getBusId(), tap.getPan(),
                date.atStartOfDay(UTC_TIME_ZONE).toEpochSecond());

        return traveller;
    }

    private static BigDecimal getFare(String fromStopId, String toStopId)
    {
        if (toStopId == null) {
            return MAX_FARE_RULES.get(fromStopId);
        }
        else {
            Map<String, BigDecimal>  toStopFares = objectMapper.convertValue(FARE_RULES.get(fromStopId), Map.class);
            return toStopFares.get(toStopId);
        }
    }

    private static boolean validateTapOnAndOff(Tap tapOn, Tap newTap) {
        // These 2 taps should be one ON and OFF
        if (!tapOn.getType().equals(newTap.getType())) {
            if (tapOn.getType().isTapOn()) {
                // Is Tap1 ON is before Tap2 OFF?
                return tapOn.getDateTimeInSecs() < newTap.getDateTimeInSecs();
            }
            else {
                // Is tap1 OFF is after Tap2 ON?
                return tapOn.getDateTimeInSecs() > newTap.getDateTimeInSecs();
            }
        }
        return false;
    }

    private static boolean validateTapsAtSameStop(Tap tapOn, Tap newTap) {
        // These 2 taps should be one ON and OFF
        if (!tapOn.getType().equals(newTap.getType())) {
            if (tapOn.getType().isTapOn()) {
                // Is Tap1 ON is before Tap2 OFF?
                return tapOn.getDateTimeInSecs() <= newTap.getDateTimeInSecs();
            }
            else {
                // Is tap1 OFF is after Tap2 ON?
                return tapOn.getDateTimeInSecs() >= newTap.getDateTimeInSecs();
            }
        }
        return false;
    }

    private static Trip createTrip(Tap tapOn, Tap tapOff, TripStatus status) {
        Trip trip = new Trip();
        trip.setBusId(tapOn.getBusId());
        trip.setCompanyId(tapOn.getCompanyId());
        trip.setPan(tapOn.getPan());
        trip.setFromStopId(tapOn.getStopId());
        trip.setStarted(tapOn.getDateTimeInSecs());
        trip.setStatus(status);

        if (tapOff != null) {
            trip.setToStopId(tapOff.getStopId());
            trip.setChargeAmount(getFare(tapOn.getStopId(), tapOff.getStopId()));
            trip.setDurationSecs(tapOff.getDateTimeInSecs() - tapOn.getDateTimeInSecs());
            trip.setFinished(tapOff.getDateTimeInSecs());
        }
        else {
            trip.setChargeAmount(getFare(tapOn.getStopId(), null));
        }

        return trip;
    }
}
