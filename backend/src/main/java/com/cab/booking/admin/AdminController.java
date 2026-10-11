package com.cab.booking.admin;

import com.cab.booking.common.ApiResponse;
import com.cab.booking.driver.DriverProfileDto;
import com.cab.booking.driver.VerificationStatus;
import com.cab.booking.ride.RideResponse;
import com.cab.booking.support.SupportTicketDto;
import com.cab.booking.user.AccountStatus;
import com.cab.booking.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Endpoints for platform management, driver approvals, ride audit, and operational analytics")
public class AdminController {

    private final AdminDashboardService adminService;
    private final com.cab.booking.driver.DriverService driverService;

    public AdminController(AdminDashboardService adminService, com.cab.booking.driver.DriverService driverService) {
        this.adminService = adminService;
        this.driverService = driverService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get high-level operations dashboard statistics and financial metrics")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDto>> getDashboardStats() {
        AdminDashboardStatsDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/users")
    @Operation(summary = "Get paginated user directory")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserDto> users = adminService.getAllUsers(PageRequest.of(page, size, Sort.by("id").descending()));
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Update user account status (ACTIVE, SUSPENDED, INACTIVE)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam AccountStatus status) {
        UserDto user = adminService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.ok("User status updated", user));
    }

    @GetMapping("/drivers/pending")
    @Operation(summary = "List drivers awaiting document verification")
    public ResponseEntity<ApiResponse<List<DriverProfileDto>>> getPendingDrivers() {
        List<DriverProfileDto> drivers = adminService.getPendingDrivers();
        return ResponseEntity.ok(ApiResponse.ok(drivers));
    }

    @PostMapping("/drivers/{driverId}/verify")
    @Operation(summary = "Approve or reject a driver's registration")
    public ResponseEntity<ApiResponse<DriverProfileDto>> verifyDriver(
            @PathVariable Long driverId,
            @RequestParam VerificationStatus status) {
        DriverProfileDto driver = adminService.verifyDriver(driverId, status);
        return ResponseEntity.ok(ApiResponse.ok("Driver verification updated to " + status, driver));
    }

    @GetMapping("/drivers/{driverId}/documents")
    @Operation(summary = "View documents submitted by a specific driver")
    public ResponseEntity<ApiResponse<List<com.cab.booking.driver.DriverDocumentDto>>> getDriverDocuments(@PathVariable Long driverId) {
        List<com.cab.booking.driver.DriverDocumentDto> docs = driverService.getDriverDocuments(driverId);
        return ResponseEntity.ok(ApiResponse.ok(docs));
    }

    @PostMapping("/documents/{documentId}/verify")
    @Operation(summary = "Approve or reject a driver's uploaded document")
    public ResponseEntity<ApiResponse<com.cab.booking.driver.DriverDocumentDto>> verifyDocument(
            @PathVariable Long documentId,
            @RequestParam VerificationStatus status,
            @RequestParam(required = false) String reason) {
        com.cab.booking.driver.DriverDocumentDto doc = driverService.verifyDocument(documentId, status, reason);
        return ResponseEntity.ok(ApiResponse.ok("Document verification updated", doc));
    }

    @GetMapping("/rides")
    @Operation(summary = "Audit all system rides across statuses")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> getAllRides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RideResponse> rides = adminService.getAllRides(PageRequest.of(page, size, Sort.by("requestedAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(rides));
    }

    @GetMapping("/tickets")
    @Operation(summary = "Get all customer support tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicketDto>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SupportTicketDto> tickets = adminService.getAllTickets(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }
}
