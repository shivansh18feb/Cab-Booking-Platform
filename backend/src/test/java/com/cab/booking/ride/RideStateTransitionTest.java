package com.cab.booking.ride;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.common.ConflictException;
import com.cab.booking.dispatch.DispatchService;
import com.cab.booking.driver.Driver;
import com.cab.booking.driver.DriverAvailability;
import com.cab.booking.driver.DriverRepository;
import com.cab.booking.driver.VehicleCategory;
import com.cab.booking.payment.PaymentRepository;
import com.cab.booking.pricing.PricingService;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.security.UserPrincipal;
import com.cab.booking.user.Role;
import com.cab.booking.user.User;
import com.cab.booking.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideStateTransitionTest {

    @Mock
    private RideRepository rideRepository;
    @Mock
    private RideEventRepository rideEventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PricingService pricingService;
    @Mock
    private DispatchService dispatchService;
    @Mock
    private RealtimeNotificationService notificationService;

    @InjectMocks
    private RideService rideService;

    private User rider;
    private User driverUser;
    private Driver driver;
    private Ride ride;
    private UserPrincipal driverPrincipal;

    @BeforeEach
    void setUp() {
        rider = new User("Rider", "rider@test.com", "+1000000001", "pass", Role.RIDER);
        rider.setId(10L);

        driverUser = new User("Driver", "driver@test.com", "+1000000002", "pass", Role.DRIVER);
        driverUser.setId(20L);

        driver = new Driver(driverUser, "DL-12345");
        driver.setId(30L);

        ride = new Ride(
                rider,
                "Times Square", 40.7580, -73.9855,
                "Empire State", 40.7484, -73.9857,
                VehicleCategory.SEDAN,
                new BigDecimal("2.50"), 10,
                new BigDecimal("12.50"), "USD"
        );
        ride.setId(100L);
        ride.setDriver(driver);
        ride.setRideStatus(RideStatus.ACCEPTED);

        driverPrincipal = new UserPrincipal(20L, "Driver", "driver@test.com", "pass", Role.DRIVER, true, Collections.emptyList());
    }

    @Test
    @DisplayName("Should progress through valid transitions: ACCEPTED -> DRIVER_ARRIVING -> DRIVER_ARRIVED -> IN_PROGRESS -> COMPLETED")
    void shouldFollowValidLifecycleTransitions() {
        when(rideRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(ride));
        when(driverRepository.findByUserId(20L)).thenReturn(Optional.of(driver));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. To DRIVER_ARRIVING
        RideResponse res1 = rideService.updateRideStatusByDriver(100L, driverPrincipal, RideStatus.DRIVER_ARRIVING);
        assertEquals(RideStatus.DRIVER_ARRIVING, res1.getRideStatus());

        // 2. To DRIVER_ARRIVED
        RideResponse res2 = rideService.updateRideStatusByDriver(100L, driverPrincipal, RideStatus.DRIVER_ARRIVED);
        assertEquals(RideStatus.DRIVER_ARRIVED, res2.getRideStatus());
        assertNotNull(res2.getArrivedAt());

        // 3. To IN_PROGRESS
        RideResponse res3 = rideService.updateRideStatusByDriver(100L, driverPrincipal, RideStatus.IN_PROGRESS);
        assertEquals(RideStatus.IN_PROGRESS, res3.getRideStatus());
        assertNotNull(res3.getStartedAt());

        // 4. To COMPLETED
        RideResponse res4 = rideService.updateRideStatusByDriver(100L, driverPrincipal, RideStatus.COMPLETED);
        assertEquals(RideStatus.COMPLETED, res4.getRideStatus());
        assertNotNull(res4.getCompletedAt());
        assertEquals(DriverAvailability.ONLINE, driver.getAvailabilityStatus(), "Driver availability should reset to ONLINE");
        assertEquals(1, driver.getTotalCompletedRides());
    }

    @Test
    @DisplayName("Should reject illegal transition: cannot jump directly from ACCEPTED to COMPLETED")
    void shouldRejectIllegalStatusTransition() {
        when(rideRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(ride));
        when(driverRepository.findByUserId(20L)).thenReturn(Optional.of(driver));

        assertThrows(BadRequestException.class, () ->
                rideService.updateRideStatusByDriver(100L, driverPrincipal, RideStatus.COMPLETED));
    }

    @Test
    @DisplayName("Should reject cancellation once trip is already IN_PROGRESS")
    void shouldRejectCancellationWhenTripInProgress() {
        ride.setRideStatus(RideStatus.IN_PROGRESS);
        when(rideRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(ride));

        UserPrincipal riderPrincipal = new UserPrincipal(10L, "Rider", "rider@test.com", "pass", Role.RIDER, true, Collections.emptyList());

        assertThrows(ConflictException.class, () ->
                rideService.cancelRide(100L, riderPrincipal, new CancelRideRequest("Change of plans")));
    }
}
