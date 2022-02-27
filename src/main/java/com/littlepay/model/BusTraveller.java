package com.littlepay.model;

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

        BusTraveller traveller = (BusTraveller) o;

        if (getDateInSecs() != traveller.getDateInSecs()) return false;
        if (!getCompanyId().equals(traveller.getCompanyId())) return false;
        if (!getBusId().equals(traveller.getBusId())) return false;
        return getPan().equals(traveller.getPan());
    }

    @Override
    public int hashCode() {
        int result = getCompanyId().hashCode();
        result = 31 * result + getBusId().hashCode();
        result = 31 * result + getPan().hashCode();
        result = 31 * result + (int) (getDateInSecs() ^ (getDateInSecs() >>> 32));
        return result;
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
