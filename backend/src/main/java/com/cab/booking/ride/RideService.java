package com.cab.booking.ride;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.common.ConflictException;
import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.dispatch.DispatchService;
import com.cab.booking.driver.Driver;
import com.cab.booking.driver.DriverAvailability;
import com.cab.booking.driver.DriverRepository;
import com.cab.booking.payment.Payment;
import com.cab.booking.payment.PaymentRepository;
import com.cab.booking.pricing.FareEstimateRequest;
import com.cab.booking.pricing.FareEstimateResponse;
import com.cab.booking.pricing.PricingService;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.security.UserPrincipal;
import com.cab.booking.user.Role;
import com.cab.booking.user.User;
import com.cab.booking.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rideRepository;
    private final RideEventRepository rideEventRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PaymentRepository paymentRepository;
    private final PricingService pricingService;
    private final DispatchService dispatchService;
    private final RealtimeNotificationService notificationService;

    public RideService(RideRepository rideRepository,
                       RideEventRepository rideEventRepository,
                       UserRepository userRepository,
                       DriverRepository driverRepository,
                       PaymentRepository paymentRepository,
                       PricingService pricingService,
                       DispatchService dispatchService,
                       RealtimeNotificationService notificationService) {
        this.rideRepository = rideRepository;
        this.rideEventRepository = rideEventRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.paymentRepository = paymentRepository;
        this.pricingService = pricingService;
        this.dispatchService = dispatchService;
        this.notificationService = notificationService;
    }

    @Transactional
    public RideResponse createRide(UserPrincipal riderPrincipal, CreateRideRequest request) {
        User rider = userRepository.findById(riderPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));

        // Check if rider already has an active ongoing ride
        rideRepository.findFirstByRiderAndRideStatusInOrderByRequestedAtDesc(
                rider, List.of(RideStatus.REQUESTED, RideStatus.ACCEPTED, RideStatus.DRIVER_ARRIVING,
                        RideStatus.DRIVER_ARRIVED, RideStatus.IN_PROGRESS)
        ).ifPresent(activeRide -> {
            throw new ConflictException("You already have an active ride in progress (Ride #" + activeRide.getId() + ")");
        });

        FareEstimateRequest estimateReq = new FareEstimateRequest(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude(),
                request.getVehicleCategory()
        );
        estimateReq.setPromoCode(request.getPromoCode());
        FareEstimateResponse estimate = pricingService.calculateEstimate(estimateReq);

        Ride ride = new Ride(
                rider,
                request.getPickupAddress().trim(),
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropoffAddress().trim(),
                request.getDropoffLatitude(),
                request.getDropoffLongitude(),
                request.getVehicleCategory(),
                estimate.getDistanceKm(),
                estimate.getEstimatedDurationMinutes(),
                estimate.getTotalEstimatedFare(),
                estimate.getCurrency()
        );
        ride = rideRepository.save(ride);

        RideEvent event = new RideEvent(
                ride,
                RideEventType.RIDE_REQUESTED.name(),
                rider,
                "RIDER",
                "Ride requested for pickup at " + ride.getPickupAddress()
        );
        rideEventRepository.save(event);

        // Notify dispatch and real-time topics
        notificationService.notifyRideStatusUpdate(ride);
        dispatchService.dispatchRideRequestToDrivers(ride);

        log.info("New ride #{} created by rider {}", ride.getId(), rider.getId());
        return RideResponse.fromEntity(ride);
    }

    @Transactional(readOnly = true)
    public RideResponse getRideDetails(Long rideId, UserPrincipal principal) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + rideId));

        validateUserCanAccessRide(ride, principal);
        return RideResponse.fromEntity(ride);
    }

    @Transactional
    public RideResponse cancelRide(Long rideId, UserPrincipal principal, CancelRideRequest request) {
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + rideId));

        validateUserCanAccessRide(ride, principal);

        if (ride.getRideStatus() == RideStatus.COMPLETED || ride.getRideStatus() == RideStatus.CANCELLED) {
            throw new ConflictException("Cannot cancel ride that is already " + ride.getRideStatus());
        }
        if (ride.getRideStatus() == RideStatus.IN_PROGRESS) {
            throw new ConflictException("Cannot cancel a trip that is already in progress");
        }

        ride.setRideStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(Instant.now());
        ride.setCancelledBy(principal.getRole().name());
        ride.setCancellationReason(request != null && request.getReason() != null ? request.getReason() : "Cancelled by user");

        // Release driver availability if assigned
        if (ride.getDriver() != null) {
            Driver driver = ride.getDriver();
            driver.setAvailabilityStatus(DriverAvailability.ONLINE);
            driverRepository.save(driver);
        }

        ride = rideRepository.save(ride);

        User actor = userRepository.findById(principal.getId()).orElse(null);
        RideEvent event = new RideEvent(
                ride,
                RideEventType.RIDE_CANCELLED.name(),
                actor,
                principal.getRole().name(),
                "Ride cancelled: " + ride.getCancellationReason()
        );
        rideEventRepository.save(event);

        notificationService.notifyRideStatusUpdate(ride);
        return RideResponse.fromEntity(ride);
    }

    @Transactional
    public RideResponse updateRideStatusByDriver(Long rideId, UserPrincipal driverPrincipal, RideStatus targetStatus) {
        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + rideId));

        Driver driver = driverRepository.findByUserId(driverPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driver.getId())) {
            throw new AccessDeniedException("You are not the assigned driver for this ride");
        }

        Instant now = Instant.now();
        RideStatus currentStatus = ride.getRideStatus();

        // State Machine validation
        switch (targetStatus) {
            case DRIVER_ARRIVING -> {
                if (currentStatus != RideStatus.ACCEPTED) {
                    throw new BadRequestException("Illegal transition: Cannot move to DRIVER_ARRIVING from " + currentStatus);
                }
                ride.setRideStatus(RideStatus.DRIVER_ARRIVING);
            }
            case DRIVER_ARRIVED -> {
                if (currentStatus != RideStatus.ACCEPTED && currentStatus != RideStatus.DRIVER_ARRIVING) {
                    throw new BadRequestException("Illegal transition: Cannot move to DRIVER_ARRIVED from " + currentStatus);
                }
                ride.setRideStatus(RideStatus.DRIVER_ARRIVED);
                ride.setArrivedAt(now);
            }
            case IN_PROGRESS -> {
                if (currentStatus != RideStatus.DRIVER_ARRIVED && currentStatus != RideStatus.DRIVER_ARRIVING) {
                    throw new BadRequestException("Illegal transition: Driver must arrive before starting trip");
                }
                ride.setRideStatus(RideStatus.IN_PROGRESS);
                ride.setStartedAt(now);
            }
            case COMPLETED -> {
                if (currentStatus != RideStatus.IN_PROGRESS) {
                    throw new BadRequestException("Illegal transition: Cannot complete ride that is not IN_PROGRESS");
                }
                ride.setRideStatus(RideStatus.COMPLETED);
                ride.setCompletedAt(now);
                ride.setFinalFare(ride.getEstimatedFare()); // Final fare locked

                // Free driver
                driver.setAvailabilityStatus(DriverAvailability.ONLINE);
                driver.setTotalCompletedRides(driver.getTotalCompletedRides() + 1);
                driverRepository.save(driver);

                // Initialize Payment record with unique idempotency key if not already created
                if (paymentRepository.findByRide(ride).isEmpty()) {
                    String idempotencyKey = "PAY-" + ride.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
                    Payment payment = new Payment(ride, ride.getFinalFare(), ride.getCurrency(), idempotencyKey);
                    paymentRepository.save(payment);
                }
            }
            default -> throw new BadRequestException("Unsupported status update: " + targetStatus);
        }

        ride = rideRepository.save(ride);

        RideEvent event = new RideEvent(
                ride,
                "RIDE_STATUS_" + targetStatus.name(),
                driver.getUser(),
                "DRIVER",
                "Ride status transitioned from " + currentStatus + " to " + targetStatus
        );
        rideEventRepository.save(event);

        notificationService.notifyRideStatusUpdate(ride);
        return RideResponse.fromEntity(ride);
    }

    @Transactional(readOnly = true)
    public Page<RideResponse> getRiderRides(UserPrincipal riderPrincipal, Pageable pageable) {
        User rider = userRepository.findById(riderPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        return rideRepository.findByRiderOrderByRequestedAtDesc(rider, pageable).map(RideResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<RideResponse> getDriverRides(UserPrincipal driverPrincipal, Pageable pageable) {
        Driver driver = driverRepository.findByUserId(driverPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        return rideRepository.findByDriverOrderByRequestedAtDesc(driver, pageable).map(RideResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public RideResponse getActiveRideForUser(UserPrincipal principal) {
        if (principal.getRole() == Role.DRIVER) {
            Driver driver = driverRepository.findByUserId(principal.getId()).orElse(null);
            if (driver == null) return null;
            return rideRepository.findFirstByDriverAndRideStatusInOrderByRequestedAtDesc(
                    driver, List.of(RideStatus.ACCEPTED, RideStatus.DRIVER_ARRIVING, RideStatus.DRIVER_ARRIVED, RideStatus.IN_PROGRESS)
            ).map(RideResponse::fromEntity).orElse(null);
        } else {
            User rider = userRepository.findById(principal.getId()).orElse(null);
            if (rider == null) return null;
            return rideRepository.findFirstByRiderAndRideStatusInOrderByRequestedAtDesc(
                    rider, List.of(RideStatus.REQUESTED, RideStatus.ACCEPTED, RideStatus.DRIVER_ARRIVING, RideStatus.DRIVER_ARRIVED, RideStatus.IN_PROGRESS)
            ).map(RideResponse::fromEntity).orElse(null);
        }
    }

    @Transactional
    public SosAlertResponse triggerSos(Long rideId, UserPrincipal principal, SosAlertRequest request) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));
        validateUserCanAccessRide(ride, principal);

        User triggerUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getId()));

        String message = request.getNote() != null && !request.getNote().isBlank()
                ? request.getNote() : "Emergency SOS alert triggered during ride";

        RideEvent sosEvent = new RideEvent(
                ride,
                "SOS_TRIGGERED",
                triggerUser,
                principal.getRole().name(),
                message
        );
        rideEventRepository.save(sosEvent);

        notificationService.broadcastSosAlert(
                rideId,
                principal.getId(),
                triggerUser.getFullName(),
                message
        );

        log.warn("SOS triggered for ride {} by user {} ({}): {}", rideId, principal.getId(), principal.getRole(), message);

        SosAlertResponse response = new SosAlertResponse();
        response.setRideId(rideId);
        response.setMessage("SOS alert sent. Emergency services have been notified.");
        response.setTriggeredAt(Instant.now());
        response.setStatus("SENT");
        response.setLatitude(request.getLatitude());
        response.setLongitude(request.getLongitude());
        return response;
    }

    private void validateUserCanAccessRide(Ride ride, UserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN) return;

        boolean isRider = ride.getRider().getId().equals(principal.getId());
        boolean isDriver = ride.getDriver() != null && ride.getDriver().getUser().getId().equals(principal.getId());

        if (!isRider && !isDriver) {
            throw new AccessDeniedException("You do not have permission to view or manipulate this ride");
        }
    }
}
