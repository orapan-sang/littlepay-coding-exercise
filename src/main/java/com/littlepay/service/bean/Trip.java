package com.littlepay.service.bean;

import java.math.BigDecimal;

public class Trip implements Comparable<Trip> {
    private long started;
    private long finished;
    private String fromStopId;
    private String toStopId;
    private String companyId;
    private String busId;
    private String pan;
    private BigDecimal chargeAmount;
    private long durationSecs;
    private TripStatus status;

    public Trip() {

    }

    public long getStarted() {
        return started;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public String getFromStopId() {
        return fromStopId;
    }

    public void setFromStopId(String fromStopId) {
        this.fromStopId = fromStopId;
    }

    public String getToStopId() {
        return toStopId;
    }

    public void setToStopId(String toStopId) {
        this.toStopId = toStopId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public BigDecimal getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(BigDecimal chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public long getDurationSecs() {
        return durationSecs;
    }

    public void setDurationSecs(long durationSecs) {
        this.durationSecs = durationSecs;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    // Overriding the compareTo method of Comparable class
    @Override
    public int compareTo(Trip trip) {
        // For Ascending order
        return Long.compare(this.getStarted(), trip.getStarted());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Trip{");
        sb.append("started=").append(started);
        sb.append(", finished=").append(finished);
        sb.append(", fromStopId='").append(fromStopId).append('\'');
        sb.append(", toStopId='").append(toStopId).append('\'');
        sb.append(", companyId='").append(companyId).append('\'');
        sb.append(", busId='").append(busId).append('\'');
        sb.append(", pan='").append(pan).append('\'');
        sb.append(", chargeAmount=").append(chargeAmount);
        sb.append(", durationSecs=").append(durationSecs);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
