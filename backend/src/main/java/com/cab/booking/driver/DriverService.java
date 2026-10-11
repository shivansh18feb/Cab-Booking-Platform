package com.cab.booking.driver;

import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideRepository;
import com.cab.booking.ride.RideResponse;
import com.cab.booking.ride.RideStatus;
import com.cab.booking.security.UserPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final RideRepository rideRepository;
    private final RealtimeNotificationService notificationService;

    public DriverService(DriverRepository driverRepository,
                         DriverDocumentRepository driverDocumentRepository,
                         RideRepository rideRepository,
                         RealtimeNotificationService notificationService) {
        this.driverRepository = driverRepository;
        this.driverDocumentRepository = driverDocumentRepository;
        this.rideRepository = rideRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public DriverProfileDto getProfile(UserPrincipal principal) {
        Driver driver = driverRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return DriverProfileDto.fromEntity(driver);
    }

    @Transactional
    public DriverProfileDto updateAvailability(UserPrincipal principal, DriverAvailability status) {
        Driver driver = driverRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        driver.setAvailabilityStatus(status);
        driver = driverRepository.save(driver);
        return DriverProfileDto.fromEntity(driver);
    }

    @Transactional
    public void updateLocation(UserPrincipal principal, UpdateLocationRequest request) {
        Driver driver = driverRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        driver.setCurrentLatitude(request.getLatitude());
        driver.setCurrentLongitude(request.getLongitude());
        driver.setLastLocationUpdate(Instant.now());
        driverRepository.save(driver);

        // If driver has an active ride or specifies activeRideId, broadcast coordinates to subscriber topic
        if (request.getActiveRideId() != null) {
            notificationService.broadcastDriverLocation(
                    request.getActiveRideId(), request.getLatitude(), request.getLongitude(),
                    request.getHeading() != null ? request.getHeading() : 0.0
            );
        }
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getAvailableRequests() {
        return rideRepository.findByRideStatus(RideStatus.REQUESTED)
                .stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverEarningsDto getEarnings(UserPrincipal principal) {
        Driver driver = driverRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        List<Ride> completedRides = rideRepository.findByDriverOrderByRequestedAtDesc(driver, Pageable.unpaged())
                .filter(r -> r.getRideStatus() == RideStatus.COMPLETED)
                .toList();

        BigDecimal gross = completedRides.stream()
                .map(r -> r.getFinalFare() != null ? r.getFinalFare() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 20% platform commission
        BigDecimal commission = gross.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(commission);

        return new DriverEarningsDto(gross, commission, net, completedRides.size(), driver.getAverageRating());
    }

    @Transactional
    public DriverDocumentDto submitDocument(UserPrincipal principal, SubmitDocumentRequest request) {
        Driver driver = driverRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        DriverDocument document = new DriverDocument(driver, request.getDocumentType(), request.getDocumentUrl().trim());
        document = driverDocumentRepository.save(document);
        return DriverDocumentDto.fromEntity(document);
    }

    @Transactional(readOnly = true)
    public List<DriverDocumentDto> getMyDocuments(UserPrincipal principal) {
        Driver driver = driverRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        return driverDocumentRepository.findByDriver(driver).stream()
                .map(DriverDocumentDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DriverDocumentDto> getDriverDocuments(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
        return driverDocumentRepository.findByDriver(driver).stream()
                .map(DriverDocumentDto::fromEntity)
                .toList();
    }

    @Transactional
    public DriverDocumentDto verifyDocument(Long documentId, VerificationStatus status, String rejectionReason) {
        DriverDocument document = driverDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        document.setVerificationStatus(status);
        document.setRejectionReason(rejectionReason);
        document.setReviewedAt(Instant.now());
        document = driverDocumentRepository.save(document);

        // If this document is approved and all other documents for driver are approved, set driver status to APPROVED
        if (status == VerificationStatus.APPROVED) {
            Driver driver = document.getDriver();
            List<DriverDocument> allDocs = driverDocumentRepository.findByDriver(driver);
            boolean allApproved = !allDocs.isEmpty() && allDocs.stream()
                    .allMatch(d -> d.getVerificationStatus() == VerificationStatus.APPROVED);
            if (allApproved) {
                driver.setVerificationStatus(VerificationStatus.APPROVED);
                driverRepository.save(driver);
            }
        }

        return DriverDocumentDto.fromEntity(document);
    }
}
