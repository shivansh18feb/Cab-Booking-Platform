package com.cab.booking.rating;

import java.time.Instant;

public class RatingResponse {

    private Long id;
    private Long rideId;
    private String riderName;
    private int stars;
    private String comment;
    private Instant createdAt;

    public RatingResponse() {}

    public static RatingResponse fromEntity(Rating rating) {
        RatingResponse res = new RatingResponse();
        res.setId(rating.getId());
        res.setRideId(rating.getRide().getId());
        res.setRiderName(rating.getRider().getFullName());
        res.setStars(rating.getStars());
        res.setComment(rating.getComment());
        res.setCreatedAt(rating.getCreatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
