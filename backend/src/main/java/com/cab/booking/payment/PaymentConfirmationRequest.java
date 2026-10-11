package com.cab.booking.payment;

import jakarta.validation.constraints.NotNull;

public class PaymentConfirmationRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    private String paymentIntentId;

    public PaymentConfirmationRequest() {}
    public PaymentConfirmationRequest(Long rideId, String paymentIntentId) {
        this.rideId = rideId;
        this.paymentIntentId = paymentIntentId;
    }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
}
