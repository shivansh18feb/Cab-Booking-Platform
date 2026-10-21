package com.cab.booking.payment;

import com.cab.booking.ride.Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRide(Ride ride);
    Optional<Payment> findByRideId(Long rideId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByProviderPaymentIntentId(String providerPaymentIntentId);
    Optional<Payment> findByProviderPaymentId(String providerPaymentId);
    long countByPaymentStatus(PaymentStatus status);

    /** DB-side aggregate — avoids loading all payment rows into memory. */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.ride.rider.id = :riderId ORDER BY p.createdAt DESC")
    Page<Payment> findByRiderId(@Param("riderId") Long riderId, Pageable pageable);
}
