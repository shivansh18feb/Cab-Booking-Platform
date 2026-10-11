package com.cab.booking.ride;

import com.cab.booking.driver.Driver;
import com.cab.booking.driver.VehicleCategory;
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
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideSosAndNotificationTest {

    @Mock
    private RideRepository rideRepository;
    @Mock
    private RideEventRepository rideEventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RealtimeNotificationService notificationService;

    @InjectMocks
    private RideService rideService;

    private User rider;
    private User driverUser;
    private Driver driver;
    private Ride ride;
    private UserPrincipal riderPrincipal;
    private UserPrincipal strangerPrincipal;

    @BeforeEach
    void setUp() {
        rider = new User("Rider Alice", "alice@cabflow.app", "+14155550001", "pass", Role.RIDER);
        rider.setId(10L);

        driverUser = new User("Driver Bob", "bob@cabflow.app", "+14155550002", "pass", Role.DRIVER);
        driverUser.setId(20L);

        driver = new Driver(driverUser, "DL-998877");
        driver.setId(30L);

        ride = new Ride(
                rider,
                "Point A", 37.77, -122.41,
                "Point B", 37.78, -122.40,
                VehicleCategory.SEDAN,
                new BigDecimal("5.00"), 15,
                new BigDecimal("20.00"), "USD"
        );
        ride.setId(101L);
        ride.setDriver(driver);
        ride.setRideStatus(RideStatus.IN_PROGRESS);

        riderPrincipal = new UserPrincipal(10L, "Rider Alice", "alice@cabflow.app", "pass", Role.RIDER, true, Collections.emptyList());
        strangerPrincipal = new UserPrincipal(99L, "Stranger", "stranger@other.app", "pass", Role.RIDER, true, Collections.emptyList());
    }

    @Test
    @DisplayName("Should trigger SOS alert for participant rider, save SOS event, and broadcast alert")
    void shouldTriggerSosForParticipantRider() {
        when(rideRepository.findById(101L)).thenReturn(Optional.of(ride));
        when(userRepository.findById(10L)).thenReturn(Optional.of(rider));
        when(rideEventRepository.save(any(RideEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        SosAlertRequest request = new SosAlertRequest();
        request.setNote("Medical emergency in vehicle");
        request.setLatitude(37.775);
        request.setLongitude(-122.405);

        SosAlertResponse response = rideService.triggerSos(101L, riderPrincipal, request);

        assertNotNull(response);
        assertEquals(101L, response.getRideId());
        assertEquals("SENT", response.getStatus());
        assertNotNull(response.getTriggeredAt());

        verify(rideEventRepository).save(any(RideEvent.class));
        verify(notificationService).broadcastSosAlert(eq(101L), eq(10L), eq("Rider Alice"), eq("Medical emergency in vehicle"));
    }

    @Test
    @DisplayName("Should prevent unauthorized non-participant from triggering SOS (IDOR protection)")
    void shouldRejectSosFromNonParticipant() {
        when(rideRepository.findById(101L)).thenReturn(Optional.of(ride));

        SosAlertRequest request = new SosAlertRequest();
        request.setNote("Fake alert");

        assertThrows(AccessDeniedException.class, () ->
                rideService.triggerSos(101L, strangerPrincipal, request));

        verify(rideEventRepository, never()).save(any());
        verify(notificationService, never()).broadcastSosAlert(any(), any(), any(), any());
    }
}
