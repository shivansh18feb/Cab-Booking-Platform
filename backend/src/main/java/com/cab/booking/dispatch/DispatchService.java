package com.cab.booking.dispatch;

import com.cab.booking.common.ConflictException;
import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.driver.Driver;
import com.cab.booking.driver.DriverAvailability;
import com.cab.booking.driver.DriverRepository;
import com.cab.booking.driver.VerificationStatus;
import com.cab.booking.pricing.PricingService;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.ride.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DispatchService {

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final RideEventRepository rideEventRepository;
    private final PricingService pricingService;
    private final RealtimeNotificationService notificationService;

    public DispatchService(DriverRepository driverRepository,
                           RideRepository rideRepository,
                           RideEventRepository rideEventRepository,
                           PricingService pricingService,
                           RealtimeNotificationService notificationService) {
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.rideEventRepository = rideEventRepository;
        this.pricingService = pricingService;
        this.notificationService = notificationService;
    }

    public List<Driver> findEligibleNearbyDrivers(double pickupLat, double pickupLon, double maxRadiusKm) {
        List<Driver> availableDrivers = driverRepository.findAvailableDrivers();

        return availableDrivers.stream()
                .filter(driver -> {
                    BigDecimal dist = pricingService.calculateHaversineDistanceKm(
                            pickupLat, pickupLon, driver.getCurrentLatitude(), driver.getCurrentLongitude());
                    return dist.doubleValue() <= maxRadiusKm;
                })
                .sorted(Comparator.comparingDouble(driver ->
                        pricingService.calculateHaversineDistanceKm(
                                pickupLat, pickupLon, driver.getCurrentLatitude(), driver.getCurrentLongitude()).doubleValue()
                ))
                .collect(Collectors.toList());
    }

    public void dispatchRideRequestToDrivers(Ride ride) {
        List<Driver> candidates = findEligibleNearbyDrivers(
                ride.getPickupLatitude(), ride.getPickupLongitude(), 15.0);

        log.info("Discovered {} eligible nearby drivers for ride {}", candidates.size(), ride.getId());

        for (Driver driver : candidates) {
            notificationService.sendDriverRideRequest(driver.getId(), ride);
        }
    }

    @Transactional
    public Ride acceptRideSafely(Long rideId, Long driverUserId) {
        Driver driver = driverRepository.findByUserId(driverUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + driverUserId));

        if (driver.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new ConflictException("Driver profile is not approved for taking rides");
        }
        if (driver.getAvailabilityStatus() == DriverAvailability.BUSY) {
            throw new ConflictException("Driver is currently on another active trip");
        }

        // Concurrency-safe atomic check-and-set:
        // Returns 1 if and only if rideId has status REQUESTED and driver IS NULL
        Instant now = Instant.now();
        int rowsUpdated = rideRepository.assignDriverSafely(
                rideId, driver, RideStatus.ACCEPTED, now, RideStatus.REQUESTED);

        if (rowsUpdated == 0) {
            // Another driver already claimed this ride concurrently, or status changed
            log.warn("Driver {} failed to claim ride {} (already accepted or cancelled)", driver.getId(), rideId);
            throw new ConflictException("Ride has already been accepted by another driver or cancelled");
        }

        // Mark driver busy
        driver.setAvailabilityStatus(DriverAvailability.BUSY);
        driverRepository.save(driver);

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        RideEvent event = new RideEvent(
                ride,
                RideEventType.DRIVER_ASSIGNED.name(),
                driver.getUser(),
                "DRIVER",
                "Ride accepted by driver " + driver.getUser().getFullName()
        );
        rideEventRepository.save(event);

        // Notify rider and driver topics
        notificationService.notifyRideStatusUpdate(ride);
        notificationService.notifyRiderDirect(ride.getRider().getId(), "Driver assigned: " + driver.getUser().getFullName());

        log.info("Ride {} successfully claimed by driver {}", rideId, driver.getId());
        return ride;
    }
}
