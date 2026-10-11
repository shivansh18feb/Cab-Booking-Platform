package com.cab.booking.payment;

import java.math.BigDecimal;

public class RazorpayOrderResponse {

    private Long paymentId;
    private Long rideId;
    private String orderId;
    private long amountInPaise;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private PaymentStatus status;

    private String riderName;
    private String riderEmail;
    private String riderPhone;

    public RazorpayOrderResponse() {}

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public long getAmountInPaise() { return amountInPaise; }
    public void setAmountInPaise(long amountInPaise) { this.amountInPaise = amountInPaise; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }

    public String getRiderEmail() { return riderEmail; }
    public void setRiderEmail(String riderEmail) { this.riderEmail = riderEmail; }

    public String getRiderPhone() { return riderPhone; }
    public void setRiderPhone(String riderPhone) { this.riderPhone = riderPhone; }
}
