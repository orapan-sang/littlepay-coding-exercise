package com.littlepay.service.bean;

public class Tap {
    // Tap ID
    private long id;
    // Tapping time (UTC) in epoch-seconds
    private long dateTimeInSecs;
    // Tap ON or OFF
    private TapType type;
    // Bus stop ID
    private String stopId;
    // Bus company ID
    private String companyId;
    // Bus ID
    private String busId;
    // Credit card number (PAN)
    private String pan;

    public Tap() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDateTimeInSecs() {
        return dateTimeInSecs;
    }

    public void setDateTimeInSecs(long dateTimeInSecs) {
        this.dateTimeInSecs = dateTimeInSecs;
    }

    public TapType getType() {
        return type;
    }

    public void setType(TapType type) {
        this.type = type;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tap{");
        sb.append("id=").append(id);
        sb.append(", dateTime=").append(dateTimeInSecs);
        sb.append(", type=").append(type);
        sb.append(", stopId='").append(stopId).append('\'');
        sb.append(", companyId='").append(companyId).append('\'');
        sb.append(", busId='").append(busId).append('\'');
        sb.append(", pan='").append(pan).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
