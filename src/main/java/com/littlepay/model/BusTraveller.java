package com.littlepay.model;

import java.util.Objects;

/**
 * A class represents a bus traveller who uses the same card on the same bus in the same day
 */
public class BusTraveller {
    // Bus company ID
    private String companyId;
    // Bus ID
    private String busId;
    // Credit card number (PAN)
    private String pan;
    // Travel date (UTC) in epoch-seconds
    private long dateInSecs;

    public BusTraveller() {

    }

    public BusTraveller(String companyId, String busId, String pan, long dateInSecs) {
        this.companyId = companyId;
        this.busId = busId;
        this.pan = pan;
        this.dateInSecs = dateInSecs;
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

    public long getDateInSecs() {
        return dateInSecs;
    }

    public void setDateInSecs(long dateInSecs) {
        this.dateInSecs = dateInSecs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusTraveller)) return false;
        BusTraveller that = (BusTraveller) o;
        return getDateInSecs() == that.getDateInSecs() && getCompanyId().equals(that.getCompanyId()) && getBusId().equals(that.getBusId()) && getPan().equals(that.getPan());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompanyId(), getBusId(), getPan(), getDateInSecs());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusTraveller{");
        sb.append("companyId='").append(companyId).append('\'');
        sb.append(", busId='").append(busId).append('\'');
        sb.append(", pan='").append(pan).append('\'');
        sb.append(", date=").append(dateInSecs);
        sb.append('}');
        return sb.toString();
    }
}
