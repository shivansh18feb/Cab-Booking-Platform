package com.cab.booking.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    long countByRole(Role role);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.failedLoginAttempts = :attempts, u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    int updateFailedLoginAttempts(@org.springframework.data.repository.query.Param("userId") Long userId,
                                  @org.springframework.data.repository.query.Param("attempts") int attempts,
                                  @org.springframework.data.repository.query.Param("lockedUntil") java.time.Instant lockedUntil);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :userId")
    int resetFailedLoginAttempts(@org.springframework.data.repository.query.Param("userId") Long userId);
}
