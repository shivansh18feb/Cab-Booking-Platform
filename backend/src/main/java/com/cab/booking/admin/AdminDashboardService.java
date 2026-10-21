package com.cab.booking.admin;

import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.driver.Driver;
import com.cab.booking.driver.DriverProfileDto;
import com.cab.booking.driver.DriverRepository;
import com.cab.booking.driver.VerificationStatus;
import com.cab.booking.payment.PaymentRepository;
import com.cab.booking.payment.PaymentStatus;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideRepository;
import com.cab.booking.ride.RideResponse;
import com.cab.booking.ride.RideStatus;
import com.cab.booking.support.SupportTicketDto;
import com.cab.booking.support.SupportTicketRepository;
import com.cab.booking.support.TicketStatus;
import com.cab.booking.user.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final PaymentRepository paymentRepository;
    private final SupportTicketRepository supportTicketRepository;

    public AdminDashboardService(UserRepository userRepository,
                                 DriverRepository driverRepository,
                                 RideRepository rideRepository,
                                 PaymentRepository paymentRepository,
                                 SupportTicketRepository supportTicketRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.rideRepository = rideRepository;
        this.paymentRepository = paymentRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getDashboardStats() {
        AdminDashboardStatsDto stats = new AdminDashboardStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalRiders(userRepository.countByRole(Role.RIDER));
        stats.setTotalDrivers(userRepository.countByRole(Role.DRIVER));
        stats.setVerifiedDrivers(driverRepository.countByVerificationStatus(VerificationStatus.APPROVED));
        stats.setPendingDrivers(driverRepository.countByVerificationStatus(VerificationStatus.PENDING));

        stats.setTotalRides(rideRepository.count());
        stats.setCompletedRides(rideRepository.countByRideStatus(RideStatus.COMPLETED));
        stats.setCancelledRides(rideRepository.countByRideStatus(RideStatus.CANCELLED));
        stats.setActiveRides(
                rideRepository.countByRideStatus(RideStatus.REQUESTED) +
                rideRepository.countByRideStatus(RideStatus.ACCEPTED) +
                rideRepository.countByRideStatus(RideStatus.DRIVER_ARRIVING) +
                rideRepository.countByRideStatus(RideStatus.DRIVER_ARRIVED) +
                rideRepository.countByRideStatus(RideStatus.IN_PROGRESS)
        );

        // DB-side aggregate — no longer loads all payments into memory
        BigDecimal revenue = paymentRepository.sumAmountByStatus(PaymentStatus.COMPLETED);
        stats.setTotalGrossRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        stats.setPendingTickets(supportTicketRepository.countByStatus(TicketStatus.OPEN) +
                               supportTicketRepository.countByStatus(TicketStatus.IN_PROGRESS));
        return stats;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDto::fromEntity);
    }

    @Transactional
    public UserDto updateUserStatus(Long userId, AccountStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setAccountStatus(status);
        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<DriverProfileDto> getPendingDrivers() {
        return driverRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream()
                .map(DriverProfileDto::fromEntity)
                .toList();
    }

    @Transactional
    public DriverProfileDto verifyDriver(Long driverId, VerificationStatus status) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + driverId));
        driver.setVerificationStatus(status);
        driver = driverRepository.save(driver);
        return DriverProfileDto.fromEntity(driver);
    }

    @Transactional(readOnly = true)
    public Page<RideResponse> getAllRides(Pageable pageable) {
        return rideRepository.findAll(pageable).map(RideResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<RideResponse> getAllRides(RideStatus status, Pageable pageable) {
        if (status == null) {
            return getAllRides(pageable);
        }
        return rideRepository.findByRideStatusOrderByRequestedAtDesc(status, pageable)
                .map(RideResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketDto> getAllTickets(Pageable pageable) {
        return supportTicketRepository.findAll(pageable).map(SupportTicketDto::fromEntity);
    }
}
