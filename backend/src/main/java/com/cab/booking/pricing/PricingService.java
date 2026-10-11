package com.cab.booking.pricing;

import com.cab.booking.driver.VehicleCategory;
import com.cab.booking.promo.DiscountType;
import com.cab.booking.promo.PromoCode;
import com.cab.booking.promo.PromoCodeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

@Service
public class PricingService {

    private final PromoCodeRepository promoCodeRepository;

    public PricingService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    public BigDecimal calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        // Multiply by 1.25 road curvature factor to realistically approximate driving route over straight-line
        double drivingDistance = Math.max(0.5, distance * 1.25);
        return BigDecimal.valueOf(drivingDistance).setScale(2, RoundingMode.HALF_UP);
    }

    public int estimateTravelTimeMinutes(BigDecimal distanceKm) {
        // Assume average city speed 30 km/h -> 2 minutes per km + 3 minutes base buffer
        int minutes = (int) Math.ceil(distanceKm.doubleValue() * 2.0) + 3;
        return Math.max(3, minutes);
    }

    public FareEstimateResponse calculateEstimate(FareEstimateRequest request) {
        BigDecimal distanceKm = calculateHaversineDistanceKm(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude()
        );
        int durationMinutes = estimateTravelTimeMinutes(distanceKm);
        VehicleCategory category = request.getVehicleCategory() != null ? request.getVehicleCategory() : VehicleCategory.SEDAN;

        BigDecimal baseFare;
        BigDecimal perKmRate;
        BigDecimal perMinRate;
        BigDecimal minFare;

        switch (category) {
            case HATCHBACK -> {
                baseFare = new BigDecimal("2.50");
                perKmRate = new BigDecimal("1.20");
                perMinRate = new BigDecimal("0.25");
                minFare = new BigDecimal("4.00");
            }
            case SUV -> {
                baseFare = new BigDecimal("5.00");
                perKmRate = new BigDecimal("2.20");
                perMinRate = new BigDecimal("0.50");
                minFare = new BigDecimal("8.00");
            }
            case PREMIUM -> {
                baseFare = new BigDecimal("7.50");
                perKmRate = new BigDecimal("3.00");
                perMinRate = new BigDecimal("0.75");
                minFare = new BigDecimal("12.00");
            }
            case SEDAN -> {
                baseFare = new BigDecimal("3.50");
                perKmRate = new BigDecimal("1.50");
                perMinRate = new BigDecimal("0.35");
                minFare = new BigDecimal("5.00");
            }
            default -> {
                baseFare = new BigDecimal("3.50");
                perKmRate = new BigDecimal("1.50");
                perMinRate = new BigDecimal("0.35");
                minFare = new BigDecimal("5.00");
            }
        }

        BigDecimal distanceCharge = distanceKm.multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal timeCharge = BigDecimal.valueOf(durationMinutes).multiply(perMinRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceFee = new BigDecimal("1.50");
        BigDecimal surgeMultiplier = new BigDecimal("1.00");

        BigDecimal subtotal = baseFare.add(distanceCharge).add(timeCharge).add(serviceFee);
        subtotal = subtotal.multiply(surgeMultiplier).setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCase(request.getPromoCode().trim());
            if (promoOpt.isPresent()) {
                PromoCode promo = promoOpt.get();
                Instant now = Instant.now();
                if (promo.isActive() && !now.isBefore(promo.getValidFrom()) && !now.isAfter(promo.getValidUntil())) {
                    if (subtotal.compareTo(promo.getMinimumFare()) >= 0) {
                        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
                            discountAmount = subtotal.multiply(promo.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                                    .setScale(2, RoundingMode.HALF_UP);
                            if (promo.getMaximumDiscount() != null && discountAmount.compareTo(promo.getMaximumDiscount()) > 0) {
                                discountAmount = promo.getMaximumDiscount();
                            }
                        } else {
                            discountAmount = promo.getDiscountValue();
                        }
                    }
                }
            }
        }

        BigDecimal total = subtotal.subtract(discountAmount);
        if (total.compareTo(minFare) < 0) {
            total = minFare;
        }

        FareEstimateResponse response = new FareEstimateResponse();
        response.setVehicleCategory(category);
        response.setDistanceKm(distanceKm);
        response.setEstimatedDurationMinutes(durationMinutes);
        response.setBaseFare(baseFare);
        response.setDistanceCharge(distanceCharge);
        response.setTimeCharge(timeCharge);
        response.setServiceFee(serviceFee);
        response.setSurgeMultiplier(surgeMultiplier);
        response.setDiscountAmount(discountAmount);
        response.setTotalEstimatedFare(total);
        response.setCurrency("USD");

        return response;
    }
}
