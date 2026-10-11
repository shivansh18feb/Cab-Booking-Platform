package com.cab.booking.pricing;

import com.cab.booking.driver.VehicleCategory;
import java.math.BigDecimal;

public class FareEstimateResponse {

    private VehicleCategory vehicleCategory;
    private BigDecimal distanceKm;
    private int estimatedDurationMinutes;
    private BigDecimal baseFare;
    private BigDecimal distanceCharge;
    private BigDecimal timeCharge;
    private BigDecimal serviceFee;
    private BigDecimal surgeMultiplier;
    private BigDecimal discountAmount;
    private BigDecimal totalEstimatedFare;
    private String currency;

    public FareEstimateResponse() {}

    public VehicleCategory getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(VehicleCategory vehicleCategory) { this.vehicleCategory = vehicleCategory; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    public BigDecimal getBaseFare() { return baseFare; }
    public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
    public BigDecimal getDistanceCharge() { return distanceCharge; }
    public void setDistanceCharge(BigDecimal distanceCharge) { this.distanceCharge = distanceCharge; }
    public BigDecimal getTimeCharge() { return timeCharge; }
    public void setTimeCharge(BigDecimal timeCharge) { this.timeCharge = timeCharge; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }
    public BigDecimal getSurgeMultiplier() { return surgeMultiplier; }
    public void setSurgeMultiplier(BigDecimal surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTotalEstimatedFare() { return totalEstimatedFare; }
    public void setTotalEstimatedFare(BigDecimal totalEstimatedFare) { this.totalEstimatedFare = totalEstimatedFare; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
