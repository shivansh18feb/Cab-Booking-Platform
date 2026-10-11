package com.cab.booking.user;

import com.cab.booking.common.ApiResponse;
import com.cab.booking.common.ResourceNotFoundException;
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
@RequestMapping("/api/v1/riders")
@PreAuthorize("hasAnyRole('RIDER', 'ADMIN')")
@Tag(name = "Rider Profile & Places", description = "Endpoints for managing saved places (Home, Work) and rider profile")
public class RiderController {

    private final UserRepository userRepository;
    private final SavedPlaceRepository savedPlaceRepository;

    public RiderController(UserRepository userRepository, SavedPlaceRepository savedPlaceRepository) {
        this.userRepository = userRepository;
        this.savedPlaceRepository = savedPlaceRepository;
    }

    @GetMapping("/saved-places")
    @Operation(summary = "Get list of rider's saved places")
    public ResponseEntity<ApiResponse<List<SavedPlace>>> getSavedPlaces(@AuthenticationPrincipal UserPrincipal principal) {
        User rider = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));
        List<SavedPlace> places = savedPlaceRepository.findByUserOrderByCreatedAtDesc(rider);
        return ResponseEntity.ok(ApiResponse.ok(places));
    }

    @PostMapping("/saved-places")
    @Operation(summary = "Add a new saved place")
    public ResponseEntity<ApiResponse<SavedPlace>> addSavedPlace(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SavedPlaceRequest request) {
        User rider = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Rider not found"));

        SavedPlace place = new SavedPlace(
                rider,
                request.getPlaceName().trim(),
                request.getAddress().trim(),
                request.getLatitude(),
                request.getLongitude()
        );
        place = savedPlaceRepository.save(place);
        return ResponseEntity.ok(ApiResponse.ok("Saved place added successfully", place));
    }

    @DeleteMapping("/saved-places/{id}")
    @Operation(summary = "Remove a saved place")
    public ResponseEntity<ApiResponse<Void>> deleteSavedPlace(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        SavedPlace place = savedPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Saved place not found"));

        if (!place.getUser().getId().equals(principal.getId()) && principal.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized to delete this place"));
        }

        savedPlaceRepository.delete(place);
        return ResponseEntity.ok(ApiResponse.ok("Saved place removed", null));
    }
}
