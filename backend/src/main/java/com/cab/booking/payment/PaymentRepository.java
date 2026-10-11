package com.cab.booking.payment;

import com.cab.booking.ride.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRide(Ride ride);
    Optional<Payment> findByRideId(Long rideId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByProviderPaymentIntentId(String providerPaymentIntentId);
    Optional<Payment> findByProviderPaymentId(String providerPaymentId);
    long countByPaymentStatus(PaymentStatus status);
}
