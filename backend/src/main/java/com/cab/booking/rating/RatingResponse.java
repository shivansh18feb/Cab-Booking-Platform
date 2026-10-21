package com.cab.booking.rating;

import java.time.Instant;

public class RatingResponse {

    private Long id;
    private Long rideId;
    private Long riderId;
    private String riderName;
    private Long driverId;
    private String driverName;
    private int stars;
    private String comment;
    private boolean isHidden;
    private Instant moderatedAt;
    private Instant createdAt;

    public RatingResponse() {}

    public static RatingResponse fromEntity(Rating rating) {
        RatingResponse res = new RatingResponse();
        res.setId(rating.getId());
        res.setRideId(rating.getRide().getId());
        res.setRiderId(rating.getRider().getId());
        res.setRiderName(rating.getRider().getFullName());
        if (rating.getDriver() != null) {
            res.setDriverId(rating.getDriver().getId());
            res.setDriverName(rating.getDriver().getUser().getFullName());
        }
        res.setStars(rating.getStars());
        res.setComment(rating.getComment());
        res.setHidden(rating.isHidden());
        res.setModeratedAt(rating.getModeratedAt());
        res.setCreatedAt(rating.getCreatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public Long getRiderId() { return riderId; }
    public void setRiderId(Long riderId) { this.riderId = riderId; }
    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }
    public Instant getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(Instant moderatedAt) { this.moderatedAt = moderatedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
