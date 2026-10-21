package com.cab.booking.payment;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {

    private Long id;
    private Long rideId;
    private String pickupAddress;
    private String dropoffAddress;
    private BigDecimal amount;
    private String currency;
    private String paymentProvider;
    private String paymentStatus;
    private String providerPaymentId;
    private String providerPaymentIntentId;
    private Instant paidAt;
    private BigDecimal refundedAmount;
    private Instant createdAt;

    public PaymentResponse() {}

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse res = new PaymentResponse();
        res.setId(payment.getId());
        if (payment.getRide() != null) {
            res.setRideId(payment.getRide().getId());
            res.setPickupAddress(payment.getRide().getPickupAddress());
            res.setDropoffAddress(payment.getRide().getDropoffAddress());
        }
        res.setAmount(payment.getAmount());
        res.setCurrency(payment.getCurrency());
        res.setPaymentProvider(payment.getPaymentProvider());
        res.setPaymentStatus(payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : null);
        res.setProviderPaymentId(payment.getProviderPaymentId());
        res.setProviderPaymentIntentId(payment.getProviderPaymentIntentId());
        res.setPaidAt(payment.getPaidAt());
        res.setRefundedAmount(payment.getRefundedAmount());
        res.setCreatedAt(payment.getCreatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
    public String getProviderPaymentIntentId() { return providerPaymentIntentId; }
    public void setProviderPaymentIntentId(String providerPaymentIntentId) { this.providerPaymentIntentId = providerPaymentIntentId; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
