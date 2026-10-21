package com.cab.booking.admin;

import com.cab.booking.audit.AuditLogDto;
import com.cab.booking.audit.AuditLogService;
import com.cab.booking.common.ApiResponse;
import com.cab.booking.driver.DriverProfileDto;
import com.cab.booking.driver.DriverService;
import com.cab.booking.driver.VerificationStatus;
import com.cab.booking.promo.CreatePromoRequest;
import com.cab.booking.promo.PromoDto;
import com.cab.booking.promo.PromoRedemptionDto;
import com.cab.booking.promo.PromoService;
import com.cab.booking.rating.RatingResponse;
import com.cab.booking.rating.RatingService;
import com.cab.booking.ride.RideResponse;
import com.cab.booking.ride.RideStatus;
import com.cab.booking.security.UserPrincipal;
import com.cab.booking.support.SupportService;
import com.cab.booking.support.SupportTicketDto;
import com.cab.booking.support.TicketStatus;
import com.cab.booking.support.UpdateTicketStatusRequest;
import com.cab.booking.user.AccountStatus;
import com.cab.booking.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Endpoints for platform management, driver approvals, ride audit, and operational analytics")
public class AdminController {

    private final AdminDashboardService adminService;
    private final DriverService driverService;
    private final SupportService supportService;
    private final PromoService promoService;
    private final RatingService ratingService;
    private final AuditLogService auditLogService;

    public AdminController(AdminDashboardService adminService,
                           DriverService driverService,
                           SupportService supportService,
                           PromoService promoService,
                           RatingService ratingService,
                           AuditLogService auditLogService) {
        this.adminService = adminService;
        this.driverService = driverService;
        this.supportService = supportService;
        this.promoService = promoService;
        this.ratingService = ratingService;
        this.auditLogService = auditLogService;
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
            @RequestParam AccountStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        UserDto user = adminService.updateUserStatus(userId, status);
        auditLogService.record(principal, "UPDATE_USER_STATUS", "USER", userId.toString(), "Status changed to " + status, null);
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
            @RequestParam VerificationStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        DriverProfileDto driver = adminService.verifyDriver(driverId, status);
        auditLogService.record(principal, "VERIFY_DRIVER", "DRIVER", driverId.toString(), "Verification set to " + status, null);
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
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        com.cab.booking.driver.DriverDocumentDto doc = driverService.verifyDocument(documentId, status, reason);
        auditLogService.record(principal, "VERIFY_DOCUMENT", "DRIVER_DOCUMENT", documentId.toString(), "Status: " + status + (reason != null ? " Reason: " + reason : ""), null);
        return ResponseEntity.ok(ApiResponse.ok("Document verification updated", doc));
    }

    @GetMapping("/rides")
    @Operation(summary = "Audit all system rides across statuses with optional status filter")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> getAllRides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RideStatus status) {
        Page<RideResponse> rides = adminService.getAllRides(status,
                PageRequest.of(page, size, Sort.by("requestedAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(rides));
    }

    // ── Support Tickets ──────────────────────────────────────────────────────────

    @GetMapping("/tickets")
    @Operation(summary = "Get all customer support tickets with optional status filter")
    public ResponseEntity<ApiResponse<Page<SupportTicketDto>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TicketStatus status) {
        Page<SupportTicketDto> tickets = supportService.getAllTickets(status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }

    @PatchMapping("/tickets/{ticketId}/status")
    @Operation(summary = "Update support ticket status, assign admin, and write resolution notes")
    public ResponseEntity<ApiResponse<SupportTicketDto>> updateTicketStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SupportTicketDto ticket = supportService.updateTicket(ticketId, request, principal);
        auditLogService.record(principal, "UPDATE_TICKET_STATUS", "SUPPORT_TICKET", ticketId.toString(),
                "Status: " + request.getStatus() + (request.getResolutionNotes() != null ? " Notes: " + request.getResolutionNotes() : ""), null);
        return ResponseEntity.ok(ApiResponse.ok("Support ticket updated", ticket));
    }

    // ── Promotions Management ───────────────────────────────────────────────────

    @GetMapping("/promotions")
    @Operation(summary = "List all promo codes with redemption stats")
    public ResponseEntity<ApiResponse<Page<PromoDto>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PromoDto> promos = promoService.getAllPromotions(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(promos));
    }

    @PostMapping("/promotions")
    @Operation(summary = "Create a new promotional discount coupon")
    public ResponseEntity<ApiResponse<PromoDto>> createPromotion(
            @Valid @RequestBody CreatePromoRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PromoDto promo = promoService.createPromotion(request);
        auditLogService.record(principal, "CREATE_PROMOTION", "PROMO_CODE", promo.getId().toString(), "Created code: " + promo.getCode(), null);
        return ResponseEntity.ok(ApiResponse.ok("Promotion created successfully", promo));
    }

    @PatchMapping("/promotions/{promoId}/toggle")
    @Operation(summary = "Activate or deactivate a promotional discount code")
    public ResponseEntity<ApiResponse<PromoDto>> togglePromotion(
            @PathVariable Long promoId,
            @AuthenticationPrincipal UserPrincipal principal) {
        PromoDto promo = promoService.togglePromotionActive(promoId);
        auditLogService.record(principal, "TOGGLE_PROMOTION", "PROMO_CODE", promoId.toString(), "Active: " + promo.isActive(), null);
        return ResponseEntity.ok(ApiResponse.ok("Promotion status toggled", promo));
    }

    @DeleteMapping("/promotions/{promoId}")
    @Operation(summary = "Delete a promotional discount code")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
            @PathVariable Long promoId,
            @AuthenticationPrincipal UserPrincipal principal) {
        promoService.deletePromotion(promoId);
        auditLogService.record(principal, "DELETE_PROMOTION", "PROMO_CODE", promoId.toString(), "Deleted promo", null);
        return ResponseEntity.ok(ApiResponse.ok("Promotion deleted successfully", null));
    }

    @GetMapping("/promotions/{promoId}/redemptions")
    @Operation(summary = "View user redemptions history for a specific promo code")
    public ResponseEntity<ApiResponse<Page<PromoRedemptionDto>>> getPromoRedemptions(
            @PathVariable Long promoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PromoRedemptionDto> redemptions = promoService.getPromoRedemptions(promoId,
                PageRequest.of(page, size, Sort.by("redeemedAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(redemptions));
    }

    // ── Review Moderation ────────────────────────────────────────────────────────

    @GetMapping("/reviews")
    @Operation(summary = "List all rider trip reviews with optional filter for stars and hidden status")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isHidden,
            @RequestParam(required = false) Integer stars) {
        Page<RatingResponse> reviews = ratingService.getAllReviewsForAdmin(isHidden, stars,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(reviews));
    }

    @PatchMapping("/reviews/{ratingId}/visibility")
    @Operation(summary = "Hide or show a user review")
    public ResponseEntity<ApiResponse<RatingResponse>> toggleReviewVisibility(
            @PathVariable Long ratingId,
            @RequestParam boolean hide,
            @AuthenticationPrincipal UserPrincipal principal) {
        RatingResponse review = ratingService.toggleReviewVisibility(ratingId, hide, principal);
        auditLogService.record(principal, "MODERATE_REVIEW_VISIBILITY", "RATING", ratingId.toString(), "Hidden: " + hide, null);
        return ResponseEntity.ok(ApiResponse.ok("Review visibility updated", review));
    }

    @DeleteMapping("/reviews/{ratingId}")
    @Operation(summary = "Delete an abusive or inappropriate review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long ratingId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ratingService.deleteReview(ratingId, principal);
        auditLogService.record(principal, "DELETE_REVIEW", "RATING", ratingId.toString(), "Deleted review", null);
        return ResponseEntity.ok(ApiResponse.ok("Review deleted successfully", null));
    }

    // ── Audit Logs ───────────────────────────────────────────────────────────────

    @GetMapping("/audit-logs")
    @Operation(summary = "View persistent administrative audit trails and security activity")
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType) {
        Page<AuditLogDto> logs = auditLogService.getAuditLogs(action, resourceType,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
