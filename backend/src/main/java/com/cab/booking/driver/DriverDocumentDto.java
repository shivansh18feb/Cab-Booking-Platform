package com.cab.booking.driver;

import java.time.Instant;

public class DriverDocumentDto {

    private Long id;
    private Long driverId;
    private DocumentType documentType;
    private String documentUrl;
    private VerificationStatus verificationStatus;
    private String rejectionReason;
    private Instant reviewedAt;
    private Instant createdAt;

    public DriverDocumentDto() {}

    public static DriverDocumentDto fromEntity(DriverDocument doc) {
        DriverDocumentDto dto = new DriverDocumentDto();
        dto.setId(doc.getId());
        dto.setDriverId(doc.getDriver().getId());
        dto.setDocumentType(doc.getDocumentType());
        dto.setDocumentUrl(doc.getDocumentUrl());
        dto.setVerificationStatus(doc.getVerificationStatus());
        dto.setRejectionReason(doc.getRejectionReason());
        dto.setReviewedAt(doc.getReviewedAt());
        dto.setCreatedAt(doc.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }
    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
