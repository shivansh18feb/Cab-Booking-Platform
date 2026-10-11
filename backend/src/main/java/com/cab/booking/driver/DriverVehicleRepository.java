package com.cab.booking.driver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverVehicleRepository extends JpaRepository<DriverVehicle, Long> {
    List<DriverVehicle> findByDriver(Driver driver);
    Optional<DriverVehicle> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
}
