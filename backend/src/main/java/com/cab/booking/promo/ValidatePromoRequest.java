package com.cab.booking.promo;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ValidatePromoRequest {

    @NotBlank(message = "Promo code is required")
    private String code;

    private BigDecimal fareAmount = BigDecimal.ZERO;

    public ValidatePromoRequest() {}
    public ValidatePromoRequest(String code, BigDecimal fareAmount) {
        this.code = code;
        this.fareAmount = fareAmount;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getFareAmount() { return fareAmount; }
    public void setFareAmount(BigDecimal fareAmount) { this.fareAmount = fareAmount; }
}
