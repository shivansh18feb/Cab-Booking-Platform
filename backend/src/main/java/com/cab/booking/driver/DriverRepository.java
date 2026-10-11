package com.cab.booking.driver;

import com.cab.booking.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUser(User user);
    Optional<Driver> findByUserId(Long userId);
    boolean existsByLicenseNumber(String licenseNumber);

    List<Driver> findByVerificationStatus(VerificationStatus status);

    @Query("SELECT d FROM Driver d WHERE d.verificationStatus = 'APPROVED' AND d.availabilityStatus = 'ONLINE' AND d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL")
    List<Driver> findAvailableDrivers();

    long countByVerificationStatus(VerificationStatus status);
}
