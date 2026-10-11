package com.cab.booking.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SubmitRatingRequest {

    @NotNull(message = "Star rating is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private Integer stars;

    private String comment;

    public SubmitRatingRequest() {}
    public SubmitRatingRequest(Integer stars, String comment) {
        this.stars = stars;
        this.comment = comment;
    }

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
