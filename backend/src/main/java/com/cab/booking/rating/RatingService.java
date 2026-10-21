package com.cab.booking.rating;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.common.ConflictException;
import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.driver.Driver;
import com.cab.booking.driver.DriverRepository;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideRepository;
import com.cab.booking.ride.RideStatus;
import com.cab.booking.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    public RatingService(RatingRepository ratingRepository, RideRepository rideRepository, DriverRepository driverRepository) {
        this.ratingRepository = ratingRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public RatingResponse submitRating(Long rideId, SubmitRatingRequest request, UserPrincipal principal) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        if (!ride.getRider().getId().equals(principal.getId())) {
            throw new BadRequestException("Only the rider who took this trip can submit a rating");
        }

        if (ride.getRideStatus() != RideStatus.COMPLETED) {
            throw new BadRequestException("Cannot rate a ride that is not COMPLETED");
        }

        if (ratingRepository.existsByRide(ride)) {
            throw new ConflictException("A rating has already been submitted for this ride");
        }

        Driver driver = ride.getDriver();
        if (driver == null) {
            throw new BadRequestException("No driver was assigned to this ride");
        }

        Rating rating = new Rating(
                ride,
                ride.getRider(),
                driver,
                request.getStars(),
                request.getComment() != null ? request.getComment().trim() : null
        );
        rating = ratingRepository.save(rating);

        // Update driver's aggregate rating
        Double avg = ratingRepository.calculateAverageRatingForDriver(driver);
        if (avg != null) {
            driver.setAverageRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            driverRepository.save(driver);
        }

        return RatingResponse.fromEntity(rating);
    }

    @Transactional(readOnly = true)
    public Page<RatingResponse> getDriverRatings(Long driverId, Pageable pageable) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        return ratingRepository.findByDriverAndIsHiddenFalseOrderByCreatedAtDesc(driver, pageable).map(RatingResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<RatingResponse> getAllReviewsForAdmin(Boolean isHidden, Integer stars, Pageable pageable) {
        if (isHidden != null && stars != null) {
            return ratingRepository.findByIsHiddenAndStarsOrderByCreatedAtDesc(isHidden, stars, pageable)
                    .map(RatingResponse::fromEntity);
        } else if (isHidden != null) {
            return ratingRepository.findByIsHiddenOrderByCreatedAtDesc(isHidden, pageable)
                    .map(RatingResponse::fromEntity);
        } else if (stars != null) {
            return ratingRepository.findByStarsOrderByCreatedAtDesc(stars, pageable)
                    .map(RatingResponse::fromEntity);
        }
        return ratingRepository.findAll(pageable).map(RatingResponse::fromEntity);
    }

    @Transactional
    public RatingResponse toggleReviewVisibility(Long ratingId, boolean hide, UserPrincipal adminPrincipal) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + ratingId));
        rating.setHidden(hide);
        rating.setModeratedAt(java.time.Instant.now());
        rating = ratingRepository.save(rating);

        // Recalculate driver average rating
        Driver driver = rating.getDriver();
        Double avg = ratingRepository.calculateAverageRatingForDriver(driver);
        driver.setAverageRating(avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(5.00));
        driverRepository.save(driver);

        return RatingResponse.fromEntity(rating);
    }

    @Transactional
    public void deleteReview(Long ratingId, UserPrincipal adminPrincipal) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + ratingId));
        Driver driver = rating.getDriver();
        ratingRepository.delete(rating);

        Double avg = ratingRepository.calculateAverageRatingForDriver(driver);
        driver.setAverageRating(avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(5.00));
        driverRepository.save(driver);
    }
}
