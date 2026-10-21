package com.cab.booking.ride;

import com.cab.booking.driver.VehicleCategory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class RideResponse {

    private Long id;
    private Long riderId;
    private String riderName;
    private String riderPhone;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String vehicleInfo;
    private String licensePlate;
    private String pickupAddress;
    private double pickupLatitude;
    private double pickupLongitude;
    private String dropoffAddress;
    private double dropoffLatitude;
    private double dropoffLongitude;
    private VehicleCategory vehicleCategory;
    private BigDecimal distanceKm;
    private int estimatedDurationMinutes;
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
    private String currency;
    private RideStatus rideStatus;
    private String cancellationReason;
    private String cancelledBy;
    private Instant requestedAt;
    private Instant scheduledFor;
    private Instant acceptedAt;
    private Instant arrivedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private List<RideEventDto> events;

    public static class RideEventDto {
        private String eventType;
        private String actorRole;
        private String description;
        private Instant createdAt;

        public RideEventDto() {}
        public RideEventDto(String eventType, String actorRole, String description, Instant createdAt) {
            this.eventType = eventType;
            this.actorRole = actorRole;
            this.description = description;
            this.createdAt = createdAt;
        }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getActorRole() { return actorRole; }
        public void setActorRole(String actorRole) { this.actorRole = actorRole; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }

    public RideResponse() {}

    public static RideResponse fromEntity(Ride ride) {
        RideResponse res = new RideResponse();
        res.setId(ride.getId());
        res.setRiderId(ride.getRider().getId());
        res.setRiderName(ride.getRider().getFullName());
        res.setRiderPhone(ride.getRider().getPhoneNumber());

        if (ride.getDriver() != null) {
            res.setDriverId(ride.getDriver().getId());
            res.setDriverName(ride.getDriver().getUser().getFullName());
            res.setDriverPhone(ride.getDriver().getUser().getPhoneNumber());
            try {
                if (ride.getDriver().getVehicles() != null && !ride.getDriver().getVehicles().isEmpty()) {
                    var vehicle = ride.getDriver().getVehicles().get(0);
                    res.setVehicleInfo(vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getColor() + ")");
                    res.setLicensePlate(vehicle.getRegistrationNumber());
                }
            } catch (Exception ignored) {
                // If vehicles proxy is uninitialized, proceed without vehicle info
            }
        }

        res.setPickupAddress(ride.getPickupAddress());
        res.setPickupLatitude(ride.getPickupLatitude());
        res.setPickupLongitude(ride.getPickupLongitude());
        res.setDropoffAddress(ride.getDropoffAddress());
        res.setDropoffLatitude(ride.getDropoffLatitude());
        res.setDropoffLongitude(ride.getDropoffLongitude());
        res.setVehicleCategory(ride.getVehicleCategory());
        res.setDistanceKm(ride.getDistanceKm());
        res.setEstimatedDurationMinutes(ride.getEstimatedDurationMinutes());
        res.setEstimatedFare(ride.getEstimatedFare());
        res.setFinalFare(ride.getFinalFare());
        res.setCurrency(ride.getCurrency());
        res.setRideStatus(ride.getRideStatus());
        res.setCancellationReason(ride.getCancellationReason());
        res.setCancelledBy(ride.getCancelledBy());
        res.setRequestedAt(ride.getRequestedAt());
        res.setScheduledFor(ride.getScheduledFor());
        res.setAcceptedAt(ride.getAcceptedAt());
        res.setArrivedAt(ride.getArrivedAt());
        res.setStartedAt(ride.getStartedAt());
        res.setCompletedAt(ride.getCompletedAt());
        res.setCancelledAt(ride.getCancelledAt());

        try {
            if (ride.getEvents() != null) {
                res.setEvents(ride.getEvents().stream()
                        .map(e -> new RideEventDto(e.getEventType(), e.getActorRole(), e.getDescription(), e.getCreatedAt()))
                        .toList());
            }
        } catch (Exception ignored) {
            // If events proxy is uninitialized outside transaction, proceed gracefully
        }

        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRiderId() { return riderId; }
    public void setRiderId(Long riderId) { this.riderId = riderId; }
    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }
    public String getRiderPhone() { return riderPhone; }
    public void setRiderPhone(String riderPhone) { this.riderPhone = riderPhone; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(double pickupLatitude) { this.pickupLatitude = pickupLatitude; }
    public double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(double pickupLongitude) { this.pickupLongitude = pickupLongitude; }
    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }
    public double getDropoffLatitude() { return dropoffLatitude; }
    public void setDropoffLatitude(double dropoffLatitude) { this.dropoffLatitude = dropoffLatitude; }
    public double getDropoffLongitude() { return dropoffLongitude; }
    public void setDropoffLongitude(double dropoffLongitude) { this.dropoffLongitude = dropoffLongitude; }
    public VehicleCategory getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(VehicleCategory vehicleCategory) { this.vehicleCategory = vehicleCategory; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    public BigDecimal getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(BigDecimal estimatedFare) { this.estimatedFare = estimatedFare; }
    public BigDecimal getFinalFare() { return finalFare; }
    public void setFinalFare(BigDecimal finalFare) { this.finalFare = finalFare; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public RideStatus getRideStatus() { return rideStatus; }
    public void setRideStatus(RideStatus rideStatus) { this.rideStatus = rideStatus; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(Instant scheduledFor) { this.scheduledFor = scheduledFor; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }
    public Instant getArrivedAt() { return arrivedAt; }
    public void setArrivedAt(Instant arrivedAt) { this.arrivedAt = arrivedAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public List<RideEventDto> getEvents() { return events; }
    public void setEvents(List<RideEventDto> events) { this.events = events; }
}
