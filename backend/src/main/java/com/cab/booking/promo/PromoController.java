package com.cab.booking.promo;

import com.cab.booking.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/promotions", "/api/v1/promos"})
@Tag(name = "Promotions", description = "Endpoints for validating coupons and checking active discounts")
public class PromoController {

    private final PromoService promoService;

    public PromoController(PromoService promoService) {
        this.promoService = promoService;
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate a promo code against current ride fare")
    public ResponseEntity<ApiResponse<PromoDto>> validatePromo(@Valid @RequestBody ValidatePromoRequest request) {
        PromoDto promo = promoService.validatePromo(request);
        return ResponseEntity.ok(ApiResponse.ok("Promo code is valid", promo));
    }

    @GetMapping("/active")
    @Operation(summary = "List all publicly available promotions")
    public ResponseEntity<ApiResponse<List<PromoDto>>> getActivePromos() {
        List<PromoDto> promos = promoService.getActivePromos();
        return ResponseEntity.ok(ApiResponse.ok(promos));
    }
}
