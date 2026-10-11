package com.cab.booking.admin;

import java.math.BigDecimal;

public class AdminDashboardStatsDto {

    private long totalUsers;
    private long totalRiders;
    private long totalDrivers;
    private long verifiedDrivers;
    private long pendingDrivers;
    private long totalRides;
    private long activeRides;
    private long completedRides;
    private long cancelledRides;
    private BigDecimal totalGrossRevenue;
    private long pendingTickets;

    public AdminDashboardStatsDto() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalRiders() { return totalRiders; }
    public void setTotalRiders(long totalRiders) { this.totalRiders = totalRiders; }
    public long getTotalDrivers() { return totalDrivers; }
    public void setTotalDrivers(long totalDrivers) { this.totalDrivers = totalDrivers; }
    public long getVerifiedDrivers() { return verifiedDrivers; }
    public void setVerifiedDrivers(long verifiedDrivers) { this.verifiedDrivers = verifiedDrivers; }
    public long getPendingDrivers() { return pendingDrivers; }
    public void setPendingDrivers(long pendingDrivers) { this.pendingDrivers = pendingDrivers; }
    public long getTotalRides() { return totalRides; }
    public void setTotalRides(long totalRides) { this.totalRides = totalRides; }
    public long getActiveRides() { return activeRides; }
    public void setActiveRides(long activeRides) { this.activeRides = activeRides; }
    public long getCompletedRides() { return completedRides; }
    public void setCompletedRides(long completedRides) { this.completedRides = completedRides; }
    public long getCancelledRides() { return cancelledRides; }
    public void setCancelledRides(long cancelledRides) { this.cancelledRides = cancelledRides; }
    public BigDecimal getTotalGrossRevenue() { return totalGrossRevenue; }
    public void setTotalGrossRevenue(BigDecimal totalGrossRevenue) { this.totalGrossRevenue = totalGrossRevenue; }
    public long getPendingTickets() { return pendingTickets; }
    public void setPendingTickets(long pendingTickets) { this.pendingTickets = pendingTickets; }
}
