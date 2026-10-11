package com.cab.booking.user;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.common.ConflictException;
import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.config.AppProperties;
import com.cab.booking.driver.*;
import com.cab.booking.security.JwtTokenProvider;
import com.cab.booking.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final DriverRepository driverRepository;
    private final DriverVehicleRepository driverVehicleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AppProperties appProperties;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       DriverRepository driverRepository,
                       DriverVehicleRepository driverVehicleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       AppProperties appProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.driverRepository = driverRepository;
        this.driverVehicleRepository = driverVehicleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.appProperties = appProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("Email address already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new ConflictException("Phone number already registered: " + request.getPhoneNumber());
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.RIDER;
        // Restrict registration to RIDER or DRIVER; ADMIN must be seeded or assigned by existing admin
        if (assignedRole == Role.ADMIN) {
            assignedRole = Role.RIDER;
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().toLowerCase().trim(),
                request.getPhoneNumber().trim(),
                passwordEncoder.encode(request.getPassword()),
                assignedRole
        );
        user = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                appProperties.getJwt().getAccessTokenExpirationMs(),
                UserDto.fromEntity(user)
        );
    }

    @Transactional
    public AuthResponse registerDriver(DriverRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("Email address already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new ConflictException("Phone number already registered: " + request.getPhoneNumber());
        }
        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber().trim())) {
            throw new ConflictException("Driver license number already registered");
        }
        if (driverVehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber().trim())) {
            throw new ConflictException("Vehicle license plate / registration already registered");
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().toLowerCase().trim(),
                request.getPhoneNumber().trim(),
                passwordEncoder.encode(request.getPassword()),
                Role.DRIVER
        );
        user = userRepository.save(user);

        Driver driver = new Driver(user, request.getLicenseNumber().trim());
        // For development convenience, we approve driver, or admin can review
        driver.setVerificationStatus(VerificationStatus.APPROVED);
        driver.setAvailabilityStatus(DriverAvailability.ONLINE);
        driver.setCurrentLatitude(40.7128); // Default demo coordinates (New York / customizable)
        driver.setCurrentLongitude(-74.0060);
        driver = driverRepository.save(driver);

        DriverVehicle vehicle = new DriverVehicle(
                driver,
                request.getVehicleType(),
                request.getMake().trim(),
                request.getModel().trim(),
                request.getYear(),
                request.getRegistrationNumber().trim().toUpperCase(),
                request.getColor().trim(),
                request.getSeatingCapacity()
        );
        driverVehicleRepository.save(vehicle);

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                appProperties.getJwt().getAccessTokenExpirationMs(),
                UserDto.fromEntity(user)
        );
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            long minutes = java.time.Duration.between(Instant.now(), user.getLockedUntil()).toMinutes() + 1;
            throw new BadRequestException("Account is temporarily locked due to 5 consecutive failed login attempts. Please try again in " + minutes + " minutes.");
        }

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            if (user != null) {
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    user.setLockedUntil(Instant.now().plus(java.time.Duration.ofMinutes(15)));
                    userRepository.save(user);
                    throw new BadRequestException("Account has been temporarily locked for 15 minutes due to 5 consecutive failed login attempts.");
                }
                userRepository.save(user);
            }
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                appProperties.getJwt().getAccessTokenExpirationMs(),
                UserDto.fromEntity(user)
        );
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        String email = request.getEmail() != null && !request.getEmail().isBlank()
                ? request.getEmail().toLowerCase().trim()
                : "google_" + UUID.randomUUID().toString().substring(0, 8) + "@cabflow.app";
        String fullName = request.getFullName() != null && !request.getFullName().isBlank()
                ? request.getFullName().trim()
                : "Google User";

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String randomPhone = "+1" + (1000000000L + (long)(Math.random() * 8999999999L));
            User newUser = new User(
                    fullName,
                    email,
                    randomPhone,
                    passwordEncoder.encode(UUID.randomUUID().toString()),
                    Role.RIDER
            );
            newUser.setEmailVerified(true);
            newUser.setAuthProvider("GOOGLE");
            return userRepository.save(newUser);
        });

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("User account is " + user.getAccountStatus());
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                appProperties.getJwt().getAccessTokenExpirationMs(),
                UserDto.fromEntity(user)
        );
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            Instant expiry = Instant.now().plus(java.time.Duration.ofMinutes(30));
            PasswordResetToken resetToken = new PasswordResetToken(user, token, expiry);
            passwordResetTokenRepository.save(resetToken);
            log.info("Password reset token generated for user {}: {}", email, token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(request.getToken().trim())
                .orElseThrow(() -> new BadRequestException("Invalid or unrecognized password reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Password reset token has already been used");
        }

        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all refresh tokens for security
        refreshTokenRepository.findAll().stream()
                .filter(rt -> rt.getUser().getId().equals(user.getId()))
                .forEach(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });

        log.info("Password successfully reset for user {}", user.getEmail());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user {}", user.getEmail());
    }

    @Transactional
    public void sendVerificationEmail(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(java.time.Duration.ofHours(24));
        EmailVerificationToken verifToken = new EmailVerificationToken(user, token, expiry);
        emailVerificationTokenRepository.save(verifToken);
        log.info("Email verification token generated for {}: {}", user.getEmail(), token);
    }

    @Transactional
    public void verifyEmail(String tokenString) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(tokenString.trim())
                .orElseThrow(() -> new BadRequestException("Invalid email verification token"));

        if (token.isUsed()) {
            throw new BadRequestException("Verification token has already been used");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
        log.info("Email successfully verified for {}", user.getEmail());
    }

    @Transactional
    public UserDto updateProfile(UpdateProfileRequest request, UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getPhoneNumber().equals(request.getPhoneNumber().trim()) &&
                userRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new ConflictException("Phone number already in use by another account");
        }

        user.setFullName(request.getFullName().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(request.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid or unrecognized refresh token"));

        if (token.isRevoked()) {
            throw new BadRequestException("Refresh token has been revoked");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new BadRequestException("Refresh token has expired. Please login again.");
        }

        User user = token.getUser();
        // Rotate refresh token
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateAccessToken(principal);
        String newRefreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                appProperties.getJwt().getAccessTokenExpirationMs(),
                UserDto.fromEntity(user)
        );
    }

    @Transactional
    public void logout(String refreshTokenString) {
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            refreshTokenRepository.findByTokenHash(refreshTokenString).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    private String createAndSaveRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusMillis(appProperties.getJwt().getRefreshTokenExpirationMs());
        RefreshToken refreshToken = new RefreshToken(user, tokenValue, expiry);
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}
