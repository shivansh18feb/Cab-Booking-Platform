package com.cab.booking.dispatch;

import com.cab.booking.common.ConflictException;
import com.cab.booking.driver.Driver;
import com.cab.booking.driver.DriverAvailability;
import com.cab.booking.driver.DriverRepository;
import com.cab.booking.driver.VerificationStatus;
import com.cab.booking.pricing.PricingService;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.ride.*;
import com.cab.booking.user.Role;
import com.cab.booking.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchConcurrencyTest {

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private RideRepository rideRepository;
    @Mock
    private RideEventRepository rideEventRepository;
    @Mock
    private PricingService pricingService;
    @Mock
    private RealtimeNotificationService notificationService;

    @InjectMocks
    private DispatchService dispatchService;

    private User driverUser1;
    private User driverUser2;
    private Driver driver1;
    private Driver driver2;
    private Ride ride;

    @BeforeEach
    void setUp() {
        driverUser1 = new User("Driver 1", "driver1@test.com", "+1001", "pass", Role.DRIVER);
        driverUser1.setId(101L);
        driver1 = new Driver(driverUser1, "DL-001");
        driver1.setId(1L);
        driver1.setVerificationStatus(VerificationStatus.APPROVED);
        driver1.setAvailabilityStatus(DriverAvailability.ONLINE);

        driverUser2 = new User("Driver 2", "driver2@test.com", "+1002", "pass", Role.DRIVER);
        driverUser2.setId(102L);
        driver2 = new Driver(driverUser2, "DL-002");
        driver2.setId(2L);
        driver2.setVerificationStatus(VerificationStatus.APPROVED);
        driver2.setAvailabilityStatus(DriverAvailability.ONLINE);

        User rider = new User("Rider", "rider@test.com", "+1003", "pass", Role.RIDER);
        rider.setId(301L);

        ride = new Ride();
        ride.setId(500L);
        ride.setRider(rider);
        ride.setRideStatus(RideStatus.REQUESTED);
    }

    @Test
    @DisplayName("Should successfully assign ride to the first driver when atomic update succeeds")
    void shouldAssignRideToFirstDriver() {
        when(driverRepository.findByUserId(101L)).thenReturn(Optional.of(driver1));
        // Atomic update returns 1 indicating status was REQUESTED and driver was successfully claimed
        when(rideRepository.assignDriverSafely(eq(500L), eq(driver1), eq(RideStatus.ACCEPTED), any(Instant.class), eq(RideStatus.REQUESTED)))
                .thenReturn(1);
        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));

        Ride accepted = dispatchService.acceptRideSafely(500L, 101L);

        assertNotNull(accepted);
        assertEquals(DriverAvailability.BUSY, driver1.getAvailabilityStatus());
        verify(rideRepository, times(1)).assignDriverSafely(eq(500L), eq(driver1), eq(RideStatus.ACCEPTED), any(Instant.class), eq(RideStatus.REQUESTED));
    }

    @Test
    @DisplayName("Should reject second concurrent driver with ConflictException when atomic update returns 0")
    void shouldRejectSecondConcurrentDriverClaim() {
        when(driverRepository.findByUserId(102L)).thenReturn(Optional.of(driver2));
        // Atomic update returns 0 indicating ride was already claimed concurrently by another driver
        when(rideRepository.assignDriverSafely(eq(500L), eq(driver2), eq(RideStatus.ACCEPTED), any(Instant.class), eq(RideStatus.REQUESTED)))
                .thenReturn(0);

        ConflictException ex = assertThrows(ConflictException.class, () ->
                dispatchService.acceptRideSafely(500L, 102L));

        assertTrue(ex.getMessage().contains("already been accepted"));
        // Ensure driver2 is NOT marked busy
        assertEquals(DriverAvailability.ONLINE, driver2.getAvailabilityStatus());
    }

    @Test
    @DisplayName("Should reject driver who is currently BUSY on another trip")
    void shouldRejectBusyDriver() {
        driver1.setAvailabilityStatus(DriverAvailability.BUSY);
        when(driverRepository.findByUserId(101L)).thenReturn(Optional.of(driver1));

        ConflictException ex = assertThrows(ConflictException.class, () ->
                dispatchService.acceptRideSafely(500L, 101L));

        assertTrue(ex.getMessage().contains("currently on another active trip"));
    }
}
