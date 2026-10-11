package com.cab.booking.ride;

public class SosAlertRequest {

    private Double latitude;
    private Double longitude;
    private String note;

    public SosAlertRequest() {}

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
