package com.cab.booking.payment;

import jakarta.validation.constraints.NotNull;

public class PaymentIntentRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    public PaymentIntentRequest() {}
    public PaymentIntentRequest(Long rideId) { this.rideId = rideId; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
}
