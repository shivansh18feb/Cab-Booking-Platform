package com.cab.booking.ride;

public class CancelRideRequest {
    private String reason;

    public CancelRideRequest() {}
    public CancelRideRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
