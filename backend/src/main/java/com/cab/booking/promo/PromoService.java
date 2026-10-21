package com.cab.booking.promo;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.common.ConflictException;
import com.cab.booking.common.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class PromoService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoRedemptionRepository promoRedemptionRepository;

    public PromoService(PromoCodeRepository promoCodeRepository,
                        PromoRedemptionRepository promoRedemptionRepository) {
        this.promoCodeRepository = promoCodeRepository;
        this.promoRedemptionRepository = promoRedemptionRepository;
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
            throw new BadRequestException("Minimum fare requirement of ₹" + promo.getMinimumFare() + " not met");
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

    @Transactional(readOnly = true)
    public Page<PromoDto> getAllPromotions(Pageable pageable) {
        return promoCodeRepository.findAll(pageable).map(PromoDto::fromEntity);
    }

    @Transactional
    public PromoDto createPromotion(CreatePromoRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (promoCodeRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Promo code '" + code + "' already exists");
        }
        if (request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new BadRequestException("Valid until timestamp must be after valid from");
        }

        PromoCode promo = new PromoCode(
                code,
                request.getDiscountType(),
                request.getDiscountValue(),
                request.getMinimumFare(),
                request.getMaximumDiscount(),
                request.getValidFrom(),
                request.getValidUntil(),
                request.getUsageLimit() > 0 ? request.getUsageLimit() : 1000
        );
        promo = promoCodeRepository.save(promo);
        return PromoDto.fromEntity(promo);
    }

    @Transactional
    public PromoDto togglePromotionActive(Long promoId) {
        PromoCode promo = promoCodeRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + promoId));
        promo.setActive(!promo.isActive());
        promo = promoCodeRepository.save(promo);
        return PromoDto.fromEntity(promo);
    }

    @Transactional
    public void deletePromotion(Long promoId) {
        PromoCode promo = promoCodeRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + promoId));
        promoCodeRepository.delete(promo);
    }

    @Transactional(readOnly = true)
    public Page<PromoRedemptionDto> getPromoRedemptions(Long promoId, Pageable pageable) {
        PromoCode promo = promoCodeRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found: " + promoId));
        return promoRedemptionRepository.findByPromoCodeOrderByRedeemedAtDesc(promo, pageable)
                .map(PromoRedemptionDto::fromEntity);
    }
}
