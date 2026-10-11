package com.cab.booking.driver;

import java.math.BigDecimal;

public class DriverEarningsDto {

    private BigDecimal totalGrossEarnings;
    private BigDecimal platformCommission;
    private BigDecimal netEarnings;
    private int completedRidesCount;
    private BigDecimal averageRating;

    public DriverEarningsDto() {}

    public DriverEarningsDto(BigDecimal totalGrossEarnings, BigDecimal platformCommission, BigDecimal netEarnings, int completedRidesCount, BigDecimal averageRating) {
        this.totalGrossEarnings = totalGrossEarnings;
        this.platformCommission = platformCommission;
        this.netEarnings = netEarnings;
        this.completedRidesCount = completedRidesCount;
        this.averageRating = averageRating;
    }

    public BigDecimal getTotalGrossEarnings() { return totalGrossEarnings; }
    public void setTotalGrossEarnings(BigDecimal totalGrossEarnings) { this.totalGrossEarnings = totalGrossEarnings; }
    public BigDecimal getPlatformCommission() { return platformCommission; }
    public void setPlatformCommission(BigDecimal platformCommission) { this.platformCommission = platformCommission; }
    public BigDecimal getNetEarnings() { return netEarnings; }
    public void setNetEarnings(BigDecimal netEarnings) { this.netEarnings = netEarnings; }
    public int getCompletedRidesCount() { return completedRidesCount; }
    public void setCompletedRidesCount(int completedRidesCount) { this.completedRidesCount = completedRidesCount; }
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
}
