package com.cab.booking.driver;

import com.cab.booking.common.ApiResponse;
import com.cab.booking.dispatch.DispatchService;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideResponse;
import com.cab.booking.ride.RideService;
import com.cab.booking.ride.RideStatus;
import com.cab.booking.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
@Tag(name = "Driver Operations", description = "Endpoints for driver availability, GPS updates, ride acceptance, and trip progression")
public class DriverController {

    private final DriverService driverService;
    private final DispatchService dispatchService;
    private final RideService rideService;

    public DriverController(DriverService driverService, DispatchService dispatchService, RideService rideService) {
        this.driverService = driverService;
        this.dispatchService = dispatchService;
        this.rideService = rideService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current driver profile, vehicle details, and rating")
    public ResponseEntity<ApiResponse<DriverProfileDto>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        DriverProfileDto profile = driverService.getProfile(principal);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PatchMapping("/me/availability")
    @Operation(summary = "Update driver availability status (ONLINE, OFFLINE)")
    public ResponseEntity<ApiResponse<DriverProfileDto>> updateAvailability(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam DriverAvailability status) {
        DriverProfileDto profile = driverService.updateAvailability(principal, status);
        return ResponseEntity.ok(ApiResponse.ok("Availability updated to " + status, profile));
    }

    @PostMapping("/me/location")
    @Operation(summary = "Publish driver real-time GPS location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateLocationRequest request) {
        driverService.updateLocation(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Location updated", null));
    }

    @GetMapping("/me/requests")
    @Operation(summary = "List open pending ride requests")
    public ResponseEntity<ApiResponse<List<RideResponse>>> getRequests() {
        List<RideResponse> requests = driverService.getAvailableRequests();
        return ResponseEntity.ok(ApiResponse.ok(requests));
    }

    @PostMapping("/rides/{rideId}/accept")
    @Operation(summary = "Accept an open ride request (concurrency safe)")
    public ResponseEntity<ApiResponse<RideResponse>> acceptRide(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Ride ride = dispatchService.acceptRideSafely(rideId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Ride accepted successfully", RideResponse.fromEntity(ride)));
    }

    @PostMapping("/rides/{rideId}/status")
    @Operation(summary = "Advance trip status (DRIVER_ARRIVING, DRIVER_ARRIVED, IN_PROGRESS, COMPLETED)")
    public ResponseEntity<ApiResponse<RideResponse>> updateRideStatus(
            @PathVariable Long rideId,
            @RequestParam RideStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        RideResponse ride = rideService.updateRideStatusByDriver(rideId, principal, status);
        return ResponseEntity.ok(ApiResponse.ok("Ride status updated to " + status, ride));
    }

    @GetMapping("/me/earnings")
    @Operation(summary = "View driver earnings breakdown (gross, commission, net)")
    public ResponseEntity<ApiResponse<DriverEarningsDto>> getEarnings(@AuthenticationPrincipal UserPrincipal principal) {
        DriverEarningsDto earnings = driverService.getEarnings(principal);
        return ResponseEntity.ok(ApiResponse.ok(earnings));
    }

    @PostMapping("/me/documents")
    @Operation(summary = "Upload or submit driver document for verification")
    public ResponseEntity<ApiResponse<DriverDocumentDto>> submitDocument(
            @Valid @RequestBody SubmitDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        DriverDocumentDto doc = driverService.submitDocument(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Document submitted for review", doc));
    }

    @GetMapping("/me/documents")
    @Operation(summary = "List driver's uploaded verification documents")
    public ResponseEntity<ApiResponse<List<DriverDocumentDto>>> getMyDocuments(@AuthenticationPrincipal UserPrincipal principal) {
        List<DriverDocumentDto> docs = driverService.getMyDocuments(principal);
        return ResponseEntity.ok(ApiResponse.ok(docs));
    }
}
