package com.cab.booking.ride;

import com.cab.booking.driver.Driver;
import com.cab.booking.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    Page<Ride> findByRiderOrderByRequestedAtDesc(User rider, Pageable pageable);

    Page<Ride> findByDriverOrderByRequestedAtDesc(Driver driver, Pageable pageable);

    Page<Ride> findByRideStatusOrderByRequestedAtDesc(RideStatus status, Pageable pageable);

    List<Ride> findByRideStatus(RideStatus status);

    List<Ride> findByRideStatusAndScheduledForLessThanEqual(RideStatus status, Instant threshold);

    Optional<Ride> findFirstByRiderAndRideStatusInOrderByRequestedAtDesc(User rider, List<RideStatus> statuses);

    Optional<Ride> findFirstByDriverAndRideStatusInOrderByRequestedAtDesc(Driver driver, List<RideStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Ride r SET r.driver = :driver, r.rideStatus = :newStatus, r.acceptedAt = :acceptedAt, r.version = r.version + 1 " +
           "WHERE r.id = :rideId AND r.rideStatus = :expectedStatus AND r.driver IS NULL")
    int assignDriverSafely(@Param("rideId") Long rideId,
                           @Param("driver") Driver driver,
                           @Param("newStatus") RideStatus newStatus,
                           @Param("acceptedAt") Instant acceptedAt,
                           @Param("expectedStatus") RideStatus expectedStatus);

    long countByRideStatus(RideStatus status);
}
