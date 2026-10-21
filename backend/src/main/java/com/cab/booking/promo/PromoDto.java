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
    private Instant validFrom;
    private Instant validUntil;
    private int usageLimit;
    private int timesUsed;
    private boolean active;

    public PromoDto() {}

    public static PromoDto fromEntity(PromoCode p) {
        PromoDto dto = new PromoDto();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        dto.setDiscountType(p.getDiscountType());
        dto.setDiscountValue(p.getDiscountValue());
        dto.setMinimumFare(p.getMinimumFare());
        dto.setMaximumDiscount(p.getMaximumDiscount());
        dto.setValidFrom(p.getValidFrom());
        dto.setValidUntil(p.getValidUntil());
        dto.setUsageLimit(p.getUsageLimit());
        dto.setTimesUsed(p.getTimesUsed());
        dto.setActive(p.isActive());
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
    public Instant getValidFrom() { return validFrom; }
    public void setValidFrom(Instant validFrom) { this.validFrom = validFrom; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
    public int getTimesUsed() { return timesUsed; }
    public void setTimesUsed(int timesUsed) { this.timesUsed = timesUsed; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
