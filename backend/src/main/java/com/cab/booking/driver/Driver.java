package com.cab.booking.driver;

import com.cab.booking.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 20)
    private DriverAvailability availabilityStatus = DriverAvailability.OFFLINE;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "last_location_update")
    private Instant lastLocationUpdate;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = new BigDecimal("5.00");

    @Column(name = "total_completed_rides", nullable = false)
    private int totalCompletedRides = 0;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DriverVehicle> vehicles = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Driver() {}

    public Driver(User user, String licenseNumber) {
        this.user = user;
        this.licenseNumber = licenseNumber;
        this.verificationStatus = VerificationStatus.PENDING;
        this.availabilityStatus = DriverAvailability.OFFLINE;
        this.averageRating = new BigDecimal("5.00");
        this.totalCompletedRides = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public DriverAvailability getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(DriverAvailability availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }
    public Instant getLastLocationUpdate() { return lastLocationUpdate; }
    public void setLastLocationUpdate(Instant lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    public int getTotalCompletedRides() { return totalCompletedRides; }
    public void setTotalCompletedRides(int totalCompletedRides) { this.totalCompletedRides = totalCompletedRides; }
    public List<DriverVehicle> getVehicles() { return vehicles; }
    public void setVehicles(List<DriverVehicle> vehicles) { this.vehicles = vehicles; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
