package com.cab.booking.promo;

import java.math.BigDecimal;
import java.time.Instant;

public class PromoRedemptionDto {

    private Long id;
    private Long promoCodeId;
    private String promoCode;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long rideId;
    private BigDecimal discountAmount;
    private Instant redeemedAt;

    public PromoRedemptionDto() {}

    public static PromoRedemptionDto fromEntity(PromoRedemption pr) {
        PromoRedemptionDto dto = new PromoRedemptionDto();
        dto.setId(pr.getId());
        dto.setPromoCodeId(pr.getPromoCode().getId());
        dto.setPromoCode(pr.getPromoCode().getCode());
        dto.setUserId(pr.getUser().getId());
        dto.setUserName(pr.getUser().getFullName());
        dto.setUserEmail(pr.getUser().getEmail());
        dto.setRideId(pr.getRide().getId());
        dto.setDiscountAmount(pr.getDiscountAmount());
        dto.setRedeemedAt(pr.getRedeemedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPromoCodeId() { return promoCodeId; }
    public void setPromoCodeId(Long promoCodeId) { this.promoCodeId = promoCodeId; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public Instant getRedeemedAt() { return redeemedAt; }
    public void setRedeemedAt(Instant redeemedAt) { this.redeemedAt = redeemedAt; }
}
