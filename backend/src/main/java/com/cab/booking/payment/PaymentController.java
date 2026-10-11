package com.cab.booking.payment;

import com.cab.booking.common.ApiResponse;
import com.cab.booking.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Endpoints for Stripe payment intent creation and confirmation")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/intent")
    @PreAuthorize("hasAnyRole('RIDER', 'ADMIN')")
    @Operation(summary = "Create Stripe PaymentIntent for a completed ride")
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PaymentIntentResponse response = paymentService.createPaymentIntent(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Payment intent initialized", response));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('RIDER', 'ADMIN')")
    @Operation(summary = "Confirm successful Stripe payment transaction")
    public ResponseEntity<ApiResponse<Payment>> confirmPayment(
            @Valid @RequestBody PaymentConfirmationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Payment payment = paymentService.confirmPayment(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed successfully", payment));
    }

    @GetMapping("/ride/{rideId}")
    @Operation(summary = "Retrieve payment status for a specific ride")
    public ResponseEntity<ApiResponse<Payment>> getPayment(@PathVariable Long rideId) {
        Payment payment = paymentService.getPaymentForRide(rideId);
        return ResponseEntity.ok(ApiResponse.ok(payment));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund a completed trip payment")
    public ResponseEntity<ApiResponse<Payment>> refundPayment(
            @PathVariable Long paymentId,
            @RequestBody(required = false) RefundPaymentRequest request) {
        java.math.BigDecimal amount = request != null ? request.getAmount() : null;
        String reason = request != null ? request.getReason() : null;
        Payment payment = paymentService.refundPayment(paymentId, amount, reason);
        return ResponseEntity.ok(ApiResponse.ok("Payment refunded successfully", payment));
    }
}
