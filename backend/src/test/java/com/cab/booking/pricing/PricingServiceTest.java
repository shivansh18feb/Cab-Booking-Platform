package com.cab.booking.pricing;

import com.cab.booking.driver.VehicleCategory;
import com.cab.booking.promo.DiscountType;
import com.cab.booking.promo.PromoCode;
import com.cab.booking.promo.PromoCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(promoCodeRepository);
    }

    @Test
    @DisplayName("Should calculate positive Haversine driving distance between coordinates")
    void shouldCalculateDistanceAccurately() {
        // From Times Square (40.7580, -73.9855) to Empire State Building (40.7484, -73.9857) ~1.0 - 1.5 km
        BigDecimal distance = pricingService.calculateHaversineDistanceKm(
                40.7580, -73.9855, 40.7484, -73.9857);

        assertNotNull(distance);
        assertTrue(distance.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(distance.doubleValue() < 3.0);
    }

    @Test
    @DisplayName("Should calculate transparent fare breakdown for SEDAN category")
    void shouldCalculateSedanFareEstimate() {
        FareEstimateRequest request = new FareEstimateRequest(
                40.7580, -73.9855,
                40.7484, -73.9857,
                VehicleCategory.SEDAN
        );

        FareEstimateResponse estimate = pricingService.calculateEstimate(request);

        assertNotNull(estimate);
        assertEquals(VehicleCategory.SEDAN, estimate.getVehicleCategory());
        assertEquals(new BigDecimal("35.00"), estimate.getBaseFare());
        assertEquals(new BigDecimal("15.00"), estimate.getServiceFee());
        assertTrue(estimate.getTotalEstimatedFare().compareTo(new BigDecimal("50.00")) >= 0,
                "Fare should be at least minimum fare (₹50.00)");
        assertEquals("INR", estimate.getCurrency());
    }

    @Test
    @DisplayName("Should apply valid promo discount to calculate reduced total fare")
    void shouldApplyPromoDiscount() {
        PromoCode promo = new PromoCode(
                "DISCOUNT20",
                DiscountType.PERCENTAGE,
                new BigDecimal("20.00"),
                new BigDecimal("5.00"),
                new BigDecimal("10.00"),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(10, ChronoUnit.DAYS),
                100
        );

        when(promoCodeRepository.findByCodeIgnoreCase("DISCOUNT20")).thenReturn(Optional.of(promo));

        FareEstimateRequest request = new FareEstimateRequest(
                40.7128, -74.0060,
                40.7589, -73.9851,
                VehicleCategory.SUV
        );
        request.setPromoCode("DISCOUNT20");

        FareEstimateResponse estimate = pricingService.calculateEstimate(request);

        assertNotNull(estimate);
        assertTrue(estimate.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0,
                "Discount amount should be positive");
    }

    @Test
    @DisplayName("Should enforce category minimum fare when distance is negligible")
    void shouldEnforceMinimumFare() {
        // Identical pickup and dropoff coords
        FareEstimateRequest request = new FareEstimateRequest(
                40.7580, -73.9855,
                40.7580, -73.9855,
                VehicleCategory.PREMIUM
        );

        FareEstimateResponse estimate = pricingService.calculateEstimate(request);

        assertNotNull(estimate);
        assertEquals(VehicleCategory.PREMIUM, estimate.getVehicleCategory());
        assertTrue(estimate.getTotalEstimatedFare().compareTo(new BigDecimal("120.00")) >= 0,
                "Premium minimum fare is ₹120.00");
    }
}
