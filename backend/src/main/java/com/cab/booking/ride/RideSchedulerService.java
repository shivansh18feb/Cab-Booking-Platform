package com.cab.booking.ride;

import com.cab.booking.dispatch.DispatchService;
import com.cab.booking.realtime.RealtimeNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Periodically scans for scheduled rides approaching their pickup window
 * (within 15 minutes) and activates them into REQUESTED status for driver dispatch.
 */
@Service
public class RideSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(RideSchedulerService.class);

    private final RideRepository rideRepository;
    private final RideEventRepository rideEventRepository;
    private final DispatchService dispatchService;
    private final RealtimeNotificationService notificationService;

    public RideSchedulerService(RideRepository rideRepository,
                                RideEventRepository rideEventRepository,
                                DispatchService dispatchService,
                                RealtimeNotificationService notificationService) {
        this.rideRepository = rideRepository;
        this.rideEventRepository = rideEventRepository;
        this.dispatchService = dispatchService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 60000) // Every 1 minute
    @Transactional
    public void activateDueScheduledRides() {
        Instant dispatchThreshold = Instant.now().plus(Duration.ofMinutes(15));
        List<Ride> dueRides = rideRepository.findByRideStatusAndScheduledForLessThanEqual(
                RideStatus.SCHEDULED, dispatchThreshold);

        if (dueRides.isEmpty()) {
            return;
        }

        log.info("Found {} scheduled rides ready for dispatch activation", dueRides.size());

        for (Ride ride : dueRides) {
            try {
                ride.setRideStatus(RideStatus.REQUESTED);
                ride.setRequestedAt(Instant.now());
                rideRepository.save(ride);

                RideEvent event = new RideEvent(
                        ride,
                        RideEventType.RIDE_REQUESTED.name(),
                        ride.getRider(),
                        "SYSTEM",
                        "Scheduled ride activated for dispatch to nearby drivers"
                );
                rideEventRepository.save(event);

                // Notify rider via WebSocket that dispatch has started
                notificationService.notifyRideStatusUpdate(ride);

                // Dispatch to eligible drivers
                dispatchService.dispatchRideRequestToDrivers(ride);

                log.info("Scheduled ride #{} activated and dispatched to drivers", ride.getId());
            } catch (Exception e) {
                log.error("Failed to activate scheduled ride #{}: {}", ride.getId(), e.getMessage(), e);
            }
        }
    }
}
