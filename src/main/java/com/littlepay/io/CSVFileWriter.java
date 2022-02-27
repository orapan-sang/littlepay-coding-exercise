package com.littlepay.io;

import com.littlepay.log.Log;
import com.littlepay.service.bean.Trip;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;

import static com.littlepay.service.ExerciseMain.*;

public class CSVFileWriter {
    private static String[] HEADER = {"Started", "Finished", "DurationSecs", "FromStopId", "ToStopId", "ChargeAmount", "CompanyId", "BusID", "PAN", "Status"};
    public static boolean writeTrips(String fileName) {
        // Sort Trips by Started (ASC)
        Collections.sort(TRIPS);

        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName),
                                              CSVWriter.DEFAULT_SEPARATOR,
                                              CSVWriter.NO_QUOTE_CHARACTER,
                                              CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                              CSVWriter.DEFAULT_LINE_END)) {
            // Write header
            writer.writeNext(HEADER);


            // Write each trip line by line
            for (Trip trip: TRIPS) {
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

    private static String EpochToString(Long epochSecs) {
        if (epochSecs != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecs), UTC_TIME_ZONE);
            return dateTime.format(DATETIME_FORMATTER);
        }
        return null;
    }
}
