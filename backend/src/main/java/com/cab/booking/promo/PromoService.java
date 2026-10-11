package com.cab.booking.promo;

import com.cab.booking.common.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class PromoService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    @Transactional(readOnly = true)
    public PromoDto validatePromo(ValidatePromoRequest request) {
        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(request.getCode().trim())
                .orElseThrow(() -> new BadRequestException("Promo code not found or invalid"));

        Instant now = Instant.now();
        if (!promo.isActive() || now.isBefore(promo.getValidFrom()) || now.isAfter(promo.getValidUntil())) {
            throw new BadRequestException("This promo code has expired or is inactive");
        }

        if (promo.getTimesUsed() >= promo.getUsageLimit()) {
            throw new BadRequestException("Promo code usage limit reached");
        }

        if (request.getFareAmount() != null && request.getFareAmount().compareTo(promo.getMinimumFare()) < 0) {
            throw new BadRequestException("Minimum fare requirement of $" + promo.getMinimumFare() + " not met");
        }

        return PromoDto.fromEntity(promo);
    }

    @Transactional(readOnly = true)
    public List<PromoDto> getActivePromos() {
        return promoCodeRepository.findAll().stream()
                .filter(p -> p.isActive() && Instant.now().isBefore(p.getValidUntil()))
                .map(PromoDto::fromEntity)
                .toList();
    }
}
