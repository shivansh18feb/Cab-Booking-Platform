package com.cab.booking.config;

import com.cab.booking.driver.*;
import com.cab.booking.promo.DiscountType;
import com.cab.booking.promo.PromoCode;
import com.cab.booking.promo.PromoCodeRepository;
import com.cab.booking.user.Role;
import com.cab.booking.user.User;
import com.cab.booking.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DriverVehicleRepository driverVehicleRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      DriverRepository driverRepository,
                      DriverVehicleRepository driverVehicleRepository,
                      PromoCodeRepository promoCodeRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.driverVehicleRepository = driverVehicleRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedRiderUser();
        seedDriverUser();
        seedPromoCodes();
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@cabflow.app")) {
            User admin = new User(
                    "Platform Administrator",
                    "admin@cabflow.app",
                    "+10000000001",
                    passwordEncoder.encode("Admin@123"),
                    Role.ADMIN
            );
            admin.setEmailVerified(true);
            userRepository.save(admin);
            log.info("Demo Admin user created: admin@cabflow.app / Admin@123");
        }
    }

    private void seedRiderUser() {
        if (!userRepository.existsByEmail("rider@cabflow.app")) {
            User rider = new User(
                    "Sarah Jenkins",
                    "rider@cabflow.app",
                    "+10000000002",
                    passwordEncoder.encode("Rider@123"),
                    Role.RIDER
            );
            rider.setEmailVerified(true);
            userRepository.save(rider);
            log.info("Demo Rider user created: rider@cabflow.app / Rider@123");
        }
    }

    private void seedDriverUser() {
        if (!userRepository.existsByEmail("driver@cabflow.app")) {
            User driverUser = new User(
                    "David Miller",
                    "driver@cabflow.app",
                    "+10000000003",
                    passwordEncoder.encode("Driver@123"),
                    Role.DRIVER
            );
            driverUser.setEmailVerified(true);
            driverUser = userRepository.save(driverUser);

            Driver driver = new Driver(driverUser, "DL-987654321");
            driver.setVerificationStatus(VerificationStatus.APPROVED);
            driver.setAvailabilityStatus(DriverAvailability.ONLINE);
            // Coordinates around New York / Times Square area for instant nearby discovery
            driver.setCurrentLatitude(40.7580);
            driver.setCurrentLongitude(-73.9855);
            driver.setLastLocationUpdate(Instant.now());
            driver.setAverageRating(new BigDecimal("4.92"));
            driver.setTotalCompletedRides(48);
            driver = driverRepository.save(driver);

            DriverVehicle vehicle = new DriverVehicle(
                    driver,
                    VehicleCategory.SEDAN,
                    "Toyota",
                    "Camry Hybrid",
                    2023,
                    "NYC-CAB-789",
                    "Silver Metallic",
                    4
            );
            driverVehicleRepository.save(vehicle);
            log.info("Demo Driver user created: driver@cabflow.app / Driver@123");
        }

        if (!userRepository.existsByEmail("driver2@cabflow.app")) {
            User driverUser2 = new User(
                    "Michael Chang",
                    "driver2@cabflow.app",
                    "+10000000004",
                    passwordEncoder.encode("Driver@123"),
                    Role.DRIVER
            );
            driverUser2.setEmailVerified(true);
            driverUser2 = userRepository.save(driverUser2);

            Driver driver2 = new Driver(driverUser2, "DL-112233445");
            driver2.setVerificationStatus(VerificationStatus.APPROVED);
            driver2.setAvailabilityStatus(DriverAvailability.ONLINE);
            driver2.setCurrentLatitude(40.7527);
            driver2.setCurrentLongitude(-73.9772);
            driver2.setLastLocationUpdate(Instant.now());
            driver2.setAverageRating(new BigDecimal("4.85"));
            driver2.setTotalCompletedRides(112);
            driver2 = driverRepository.save(driver2);

            DriverVehicle vehicle2 = new DriverVehicle(
                    driver2,
                    VehicleCategory.SUV,
                    "Honda",
                    "CR-V",
                    2024,
                    "NYC-SUV-456",
                    "Midnight Black",
                    6
            );
            driverVehicleRepository.save(vehicle2);
            log.info("Demo Driver 2 user created: driver2@cabflow.app / Driver@123");
        }
    }

    private void seedPromoCodes() {
        if (promoCodeRepository.findByCodeIgnoreCase("WELCOME20").isEmpty()) {
            PromoCode promo = new PromoCode(
                    "WELCOME20",
                    DiscountType.PERCENTAGE,
                    new BigDecimal("20.00"),
                    new BigDecimal("10.00"),
                    new BigDecimal("15.00"),
                    Instant.now().minus(1, ChronoUnit.DAYS),
                    Instant.now().plus(90, ChronoUnit.DAYS),
                    5000
            );
            promoCodeRepository.save(promo);
            log.info("Demo Promo Code WELCOME20 seeded");
        }
    }
}
