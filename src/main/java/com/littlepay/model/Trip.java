package com.littlepay.model;

import java.math.BigDecimal;

public class Trip implements Comparable<Trip> {
    private long started;
    private Long finished;
    private String fromStopId;
    private String toStopId;
    private String companyId;
    private String busId;
    private String pan;
    private BigDecimal chargeAmount;
    private Long durationSecs;
    private TripStatus status;

    public Trip() {

    }

    public long getStarted() {
        return started;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    public Long getFinished() {
        return finished;
    }

    public void setFinished(Long finished) {
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

    public Long getDurationSecs() {
        return durationSecs;
    }

    public void setDurationSecs(Long durationSecs) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trip)) return false;

        Trip trip = (Trip) o;

        if (getStarted() != trip.getStarted()) return false;
        if (getFinished() != null ? !getFinished().equals(trip.getFinished()) : trip.getFinished() != null)
            return false;
        if (!getFromStopId().equals(trip.getFromStopId())) return false;
        if (getToStopId() != null ? !getToStopId().equals(trip.getToStopId()) : trip.getToStopId() != null)
            return false;
        if (!getCompanyId().equals(trip.getCompanyId())) return false;
        if (!getBusId().equals(trip.getBusId())) return false;
        if (!getPan().equals(trip.getPan())) return false;
        if (!getChargeAmount().equals(trip.getChargeAmount())) return false;
        if (getDurationSecs() != null ? !getDurationSecs().equals(trip.getDurationSecs()) : trip.getDurationSecs() != null)
            return false;
        return getStatus() == trip.getStatus();
    }

    @Override
    public int hashCode() {
        int result = (int) (getStarted() ^ (getStarted() >>> 32));
        result = 31 * result + (getFinished() != null ? getFinished().hashCode() : 0);
        result = 31 * result + getFromStopId().hashCode();
        result = 31 * result + (getToStopId() != null ? getToStopId().hashCode() : 0);
        result = 31 * result + getCompanyId().hashCode();
        result = 31 * result + getBusId().hashCode();
        result = 31 * result + getPan().hashCode();
        result = 31 * result + getChargeAmount().hashCode();
        result = 31 * result + (getDurationSecs() != null ? getDurationSecs().hashCode() : 0);
        result = 31 * result + getStatus().hashCode();
        return result;
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
