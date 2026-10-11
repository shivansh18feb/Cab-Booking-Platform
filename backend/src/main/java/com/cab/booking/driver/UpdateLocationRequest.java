package com.cab.booking.driver;

import jakarta.validation.constraints.NotNull;

public class UpdateLocationRequest {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double heading = 0.0;
    private Long activeRideId;

    public UpdateLocationRequest() {}

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }
    public Long getActiveRideId() { return activeRideId; }
    public void setActiveRideId(Long activeRideId) { this.activeRideId = activeRideId; }
}
