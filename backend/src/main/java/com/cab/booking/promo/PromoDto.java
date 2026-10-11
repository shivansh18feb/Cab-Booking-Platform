package com.cab.booking.promo;

import java.math.BigDecimal;
import java.time.Instant;

public class PromoDto {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumFare;
    private BigDecimal maximumDiscount;
    private Instant validUntil;

    public PromoDto() {}

    public static PromoDto fromEntity(PromoCode p) {
        PromoDto dto = new PromoDto();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        dto.setDiscountType(p.getDiscountType());
        dto.setDiscountValue(p.getDiscountValue());
        dto.setMinimumFare(p.getMinimumFare());
        dto.setMaximumDiscount(p.getMaximumDiscount());
        dto.setValidUntil(p.getValidUntil());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinimumFare() { return minimumFare; }
    public void setMinimumFare(BigDecimal minimumFare) { this.minimumFare = minimumFare; }
    public BigDecimal getMaximumDiscount() { return maximumDiscount; }
    public void setMaximumDiscount(BigDecimal maximumDiscount) { this.maximumDiscount = maximumDiscount; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
}
