package com.cab.booking.ride;

import java.time.Instant;

public class SosAlertResponse {

    private Long rideId;
    private Instant triggeredAt;
    private String status;
    private String message;
    private Double latitude;
    private Double longitude;

    public SosAlertResponse() {}

    public SosAlertResponse(Long rideId, Instant triggeredAt, String status, String message, Double latitude, Double longitude) {
        this.rideId = rideId;
        this.triggeredAt = triggeredAt;
        this.status = status;
        this.message = message;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
