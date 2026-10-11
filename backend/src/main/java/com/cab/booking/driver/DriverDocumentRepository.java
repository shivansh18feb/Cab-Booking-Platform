package com.cab.booking.driver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {
    List<DriverDocument> findByDriver(Driver driver);
    List<DriverDocument> findByVerificationStatus(VerificationStatus status);
}
