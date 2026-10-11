package com.cab.booking.ride;

import com.cab.booking.common.ApiResponse;
import com.cab.booking.pricing.FareEstimateRequest;
import com.cab.booking.pricing.FareEstimateResponse;
import com.cab.booking.pricing.PricingService;
import com.cab.booking.security.UserPrincipal;
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

@RestController
@RequestMapping("/api/v1/rides")
@Tag(name = "Rides", description = "Endpoints for ride booking, estimation, cancellation, and tracking")
public class RideController {

    private final RideService rideService;
    private final PricingService pricingService;

    public RideController(RideService rideService, PricingService pricingService) {
        this.rideService = rideService;
        this.pricingService = pricingService;
    }

    @PostMapping("/estimate")
    @Operation(summary = "Calculate transparent fare estimate and travel time")
    public ResponseEntity<ApiResponse<FareEstimateResponse>> getEstimate(@Valid @RequestBody FareEstimateRequest request) {
        FareEstimateResponse estimate = pricingService.calculateEstimate(request);
        return ResponseEntity.ok(ApiResponse.ok(estimate));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RIDER', 'ADMIN')")
    @Operation(summary = "Book a new ride")
    public ResponseEntity<ApiResponse<RideResponse>> createRide(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateRideRequest request) {
        RideResponse response = rideService.createRide(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Ride requested successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride details and tracking information")
    public ResponseEntity<ApiResponse<RideResponse>> getRide(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        RideResponse response = rideService.getRideDetails(id, principal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an active ride")
    public ResponseEntity<ApiResponse<RideResponse>> cancelRide(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) CancelRideRequest request) {
        RideResponse response = rideService.cancelRide(id, principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Ride cancelled successfully", response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get user's current in-progress or requested ride")
    public ResponseEntity<ApiResponse<RideResponse>> getActiveRide(@AuthenticationPrincipal UserPrincipal principal) {
        RideResponse response = rideService.getActiveRideForUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get paginated ride history for rider or driver")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> getRideHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 50), Sort.by("requestedAt").descending());
        Page<RideResponse> history = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"))
                ? rideService.getDriverRides(principal, pageRequest)
                : rideService.getRiderRides(principal, pageRequest);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @PostMapping("/{id}/sos")
    @PreAuthorize("hasAnyRole('RIDER', 'DRIVER', 'ADMIN')")
    @Operation(summary = "Trigger an SOS emergency alert for a ride in progress")
    public ResponseEntity<ApiResponse<SosAlertResponse>> triggerSos(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) SosAlertRequest request) {
        SosAlertRequest req = request != null ? request : new SosAlertRequest();
        SosAlertResponse response = rideService.triggerSos(id, principal, req);
        return ResponseEntity.ok(ApiResponse.ok("SOS alert triggered", response));
    }
}
