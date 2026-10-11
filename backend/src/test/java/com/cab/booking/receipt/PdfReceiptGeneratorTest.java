package com.cab.booking.receipt;

import com.cab.booking.driver.Driver;
import com.cab.booking.driver.VehicleCategory;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideRepository;
import com.cab.booking.ride.RideStatus;
import com.cab.booking.user.Role;
import com.cab.booking.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfReceiptGeneratorTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private PdfReceiptGeneratorService pdfReceiptGeneratorService;

    @Test
    @DisplayName("Should generate valid binary PDF receipt document for completed trip")
    void shouldGenerateValidPdfBytes() {
        User rider = new User("John Doe", "john@test.com", "+1234567890", "pass", Role.RIDER);
        rider.setId(1L);

        User driverUser = new User("Jane Smith", "jane@test.com", "+1987654321", "pass", Role.DRIVER);
        driverUser.setId(2L);

        Driver driver = new Driver(driverUser, "DL-TEST-99");
        driver.setId(5L);

        Ride ride = new Ride(
                rider,
                "Central Park West", 40.785091, -73.968285,
                "Battery Park", 40.703277, -74.017028,
                VehicleCategory.SEDAN,
                new BigDecimal("10.50"), 25,
                new BigDecimal("22.50"), "USD"
        );
        ride.setId(999L);
        ride.setDriver(driver);
        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(Instant.now());

        when(rideRepository.findById(999L)).thenReturn(Optional.of(ride));

        byte[] pdfBytes = pdfReceiptGeneratorService.generateRideReceiptPdf(999L);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "PDF should contain valid byte stream");
        // PDF files always begin with magic bytes "%PDF-"
        String header = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", header);
    }
}
