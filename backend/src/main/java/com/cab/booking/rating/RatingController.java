package com.cab.booking.rating;

import com.cab.booking.common.ApiResponse;
import com.cab.booking.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Ratings & Reviews", description = "Endpoints for submitting and viewing trip reviews")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/rides/{rideId}/rating")
    @PreAuthorize("hasAnyRole('RIDER', 'ADMIN')")
    @Operation(summary = "Submit a 1-5 star rating and feedback comment for a completed trip")
    public ResponseEntity<ApiResponse<RatingResponse>> submitRating(
            @PathVariable Long rideId,
            @Valid @RequestBody SubmitRatingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RatingResponse response = ratingService.submitRating(rideId, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Rating submitted successfully", response));
    }

    @GetMapping("/drivers/{driverId}/ratings")
    @Operation(summary = "Get reviews and rating history for a driver")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getDriverRatings(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<RatingResponse> ratings = ratingService.getDriverRatings(driverId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.ok(ratings));
    }
}
