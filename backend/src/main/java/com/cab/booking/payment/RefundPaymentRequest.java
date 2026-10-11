package com.cab.booking.payment;

import java.math.BigDecimal;

public class RefundPaymentRequest {

    private BigDecimal amount;
    private String reason;

    public RefundPaymentRequest() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
