package com.cab.booking.rating;

import com.cab.booking.driver.Driver;
import com.cab.booking.ride.Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByRide(Ride ride);
    boolean existsByRide(Ride ride);
    Page<Rating> findByDriverOrderByCreatedAtDesc(Driver driver, Pageable pageable);
    Page<Rating> findByDriverAndIsHiddenFalseOrderByCreatedAtDesc(Driver driver, Pageable pageable);

    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.driver = :driver AND r.isHidden = false")
    Double calculateAverageRatingForDriver(@Param("driver") Driver driver);

    long countByDriver(Driver driver);

    Page<Rating> findByIsHiddenOrderByCreatedAtDesc(boolean isHidden, Pageable pageable);
    Page<Rating> findByStarsOrderByCreatedAtDesc(int stars, Pageable pageable);
    Page<Rating> findByIsHiddenAndStarsOrderByCreatedAtDesc(boolean isHidden, int stars, Pageable pageable);
}
