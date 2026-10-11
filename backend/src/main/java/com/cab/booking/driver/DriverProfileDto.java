package com.cab.booking.driver;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class DriverProfileDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String licenseNumber;
    private VerificationStatus verificationStatus;
    private DriverAvailability availabilityStatus;
    private Double currentLatitude;
    private Double currentLongitude;
    private BigDecimal averageRating;
    private int totalCompletedRides;
    private List<VehicleDto> vehicles;

    public static class VehicleDto {
        private Long id;
        private VehicleCategory vehicleType;
        private String make;
        private String model;
        private int year;
        private String registrationNumber;
        private String color;
        private int seatingCapacity;

        public VehicleDto() {}
        public VehicleDto(Long id, VehicleCategory vehicleType, String make, String model, int year, String registrationNumber, String color, int seatingCapacity) {
            this.id = id;
            this.vehicleType = vehicleType;
            this.make = make;
            this.model = model;
            this.year = year;
            this.registrationNumber = registrationNumber;
            this.color = color;
            this.seatingCapacity = seatingCapacity;
        }

        public Long getId() { return id; }
        public VehicleCategory getVehicleType() { return vehicleType; }
        public String getMake() { return make; }
        public String getModel() { return model; }
        public int getYear() { return year; }
        public String getRegistrationNumber() { return registrationNumber; }
        public String getColor() { return color; }
        public int getSeatingCapacity() { return seatingCapacity; }
    }

    public DriverProfileDto() {}

    public static DriverProfileDto fromEntity(Driver driver) {
        DriverProfileDto dto = new DriverProfileDto();
        dto.setId(driver.getId());
        dto.setUserId(driver.getUser().getId());
        dto.setFullName(driver.getUser().getFullName());
        dto.setEmail(driver.getUser().getEmail());
        dto.setPhoneNumber(driver.getUser().getPhoneNumber());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setVerificationStatus(driver.getVerificationStatus());
        dto.setAvailabilityStatus(driver.getAvailabilityStatus());
        dto.setCurrentLatitude(driver.getCurrentLatitude());
        dto.setCurrentLongitude(driver.getCurrentLongitude());
        dto.setAverageRating(driver.getAverageRating());
        dto.setTotalCompletedRides(driver.getTotalCompletedRides());

        if (driver.getVehicles() != null) {
            dto.setVehicles(driver.getVehicles().stream()
                    .map(v -> new VehicleDto(v.getId(), v.getVehicleType(), v.getMake(), v.getModel(), v.getYear(), v.getRegistrationNumber(), v.getColor(), v.getSeatingCapacity()))
                    .toList());
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
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
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    public int getTotalCompletedRides() { return totalCompletedRides; }
    public void setTotalCompletedRides(int totalCompletedRides) { this.totalCompletedRides = totalCompletedRides; }
    public List<VehicleDto> getVehicles() { return vehicles; }
    public void setVehicles(List<VehicleDto> vehicles) { this.vehicles = vehicles; }
}
