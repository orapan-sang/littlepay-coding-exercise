package com.littlepay.service;

import com.littlepay.model.*;
import com.littlepay.log.Log;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Initialize this class with FareRuleMatrix to process taps from CSV file
 * and return a trip list, which can be exported to a CSV file.
 */
public class TripBuilder {
    private DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private ZoneOffset UTC_TIME_ZONE = ZoneOffset.UTC;
    private String[] HEADER = {"Started", "Finished", "DurationSecs", "FromStopId", "ToStopId", "ChargeAmount", "CompanyId", "BusID", "PAN", "Status"};

    // Map of bus traveller who used the same card on the same bus in the same day and Tap ON
    protected Map<BusTraveller, Tap> BUS_TRAVELLER_TAP_ON = new HashMap<>();

    private FareRuleMatrix fareRuleMatrix;

    public TripBuilder(FareRuleMatrix fareRuleMatrix) {
        this.fareRuleMatrix = fareRuleMatrix;
    }

    public List<Trip> loadAndProcessTaps(String fileName) {
        int count = 0;
        List<Trip> trips = new ArrayList<>();
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

                    Trip trip = processTap(tap);
                    if (trip != null)
                        trips.add(trip);

                    count++;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.warning("Cannot read tap: {0}", new Object[]{Arrays.toString(row)}, e);
                }
            }
            trips.addAll(finalizeIncompleteTrip());
        }
        catch (Exception e) {
            Log.warning("Cannot read {0}", new Object[]{fileName}, e);
        }
        return trips;
    }

    protected Trip processTap(Tap newTap) {
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

    protected List<Trip> finalizeIncompleteTrip() {
        List<Trip> incompleteTrips = new ArrayList<>();
        // Read all incomplete taps left in BUS_TRAVELLER_TAP_ON
        for (Map.Entry<BusTraveller, Tap> entry : BUS_TRAVELLER_TAP_ON.entrySet()) {
            Trip trip = createTrip(entry.getValue(), null, TripStatus.INCOMPLETE);
            incompleteTrips.add(trip);
        }
        // Clean up BUS_TRAVELLER_TAP_ON
        BUS_TRAVELLER_TAP_ON.clear();
        return incompleteTrips;
    }

    public boolean exportTripsToCsv(String fileName, List<Trip> trips) {
        // Sort Trips by Started (ASC)
        Collections.sort(trips);

        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            // Write header
            writer.writeNext(HEADER);


            // Write each trip line by line
            for (Trip trip: trips) {
                String[] row = new String[0];
                try {
                    row = new String[]{EpochToString(trip.getStarted()), EpochToString(trip.getFinished()),
                            trip.getDurationSecs() != null? String.valueOf(trip.getDurationSecs()): null, trip.getFromStopId(),
                            trip.getToStopId(), trip.getChargeAmount().toString(),
                            trip.getCompanyId(), trip.getBusId(),
                            trip.getPan(), trip.getStatus().toString()};
                    writer.writeNext(row);
                }
                catch (Exception e) {
                    Log.warning("Cannot write trip: {0}", new Object[]{row}, e);
                }
            }
        } catch (Exception e) {
            Log.warning("Cannot write {0}", new Object[]{fileName}, e);
        }

        return true;
    }

    private String EpochToString(Long epochSecs) {
        if (epochSecs != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecs), UTC_TIME_ZONE);
            return dateTime.format(DATETIME_FORMATTER);
        }
        return null;
    }

    private Trip matchTapsToTrip(Tap tapOn, Tap newTap) {
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

    private BusTraveller getBusTraveller(Tap tap) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(tap.getDateTimeInSecs()), UTC_TIME_ZONE);
        LocalDate date = dateTime.toLocalDate();
        BusTraveller traveller = new BusTraveller(tap.getCompanyId(), tap.getBusId(), tap.getPan(),
                date.atStartOfDay(UTC_TIME_ZONE).toEpochSecond());

        return traveller;
    }

    private boolean validateTapOnAndOff(Tap tapOn, Tap newTap) {
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

    private boolean validateTapsAtSameStop(Tap tapOn, Tap newTap) {
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

    private Trip createTrip(Tap tapOn, Tap tapOff, TripStatus status) {
        Trip trip = new Trip();
        trip.setBusId(tapOn.getBusId());
        trip.setCompanyId(tapOn.getCompanyId());
        trip.setPan(tapOn.getPan());
        trip.setFromStopId(tapOn.getStopId());
        trip.setStarted(tapOn.getDateTimeInSecs());
        trip.setStatus(status);

        if (tapOff != null) {
            trip.setToStopId(tapOff.getStopId());
            trip.setChargeAmount(fareRuleMatrix.getFare(tapOn.getStopId(), tapOff.getStopId()));
            trip.setDurationSecs(tapOff.getDateTimeInSecs() - tapOn.getDateTimeInSecs());
            trip.setFinished(tapOff.getDateTimeInSecs());
        }
        else {
            trip.setChargeAmount(fareRuleMatrix.getFare(tapOn.getStopId(), null));
        }

        return trip;
    }
}
