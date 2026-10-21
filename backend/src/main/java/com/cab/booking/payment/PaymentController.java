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
@Tag(name = "Payments", description = "Endpoints for Razorpay order creation, payment verification, and refunds")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping({"/create-order", "/intent"})
    @PreAuthorize("hasAnyRole('RIDER', 'ADMIN')")
    @Operation(summary = "Create Razorpay Order for a completed ride")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(
            @Valid @RequestBody PaymentIntentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RazorpayOrderResponse response = paymentService.createOrder(request.getRideId(), principal);
        return ResponseEntity.ok(ApiResponse.ok("Razorpay order created successfully", response));
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('RIDER', 'ADMIN')")
    @Operation(summary = "Verify Razorpay checkout payment signature and complete transaction")
    public ResponseEntity<ApiResponse<Payment>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Payment payment = paymentService.verifyPayment(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Payment verified and completed successfully", payment));
    }

    @GetMapping("/ride/{rideId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retrieve payment status for a specific ride")
    public ResponseEntity<ApiResponse<Payment>> getPayment(
            @PathVariable Long rideId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Payment payment = paymentService.getPaymentForRide(rideId, principal);
        return ResponseEntity.ok(ApiResponse.ok(payment));
    }

    @GetMapping("/my-payments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retrieve paginated payment transactions for the authenticated rider")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<PaymentResponse> payments = paymentService.getMyPayments(
                principal,
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(ApiResponse.ok(payments));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund a completed trip payment via Razorpay")
    public ResponseEntity<ApiResponse<Payment>> refundPayment(
            @PathVariable Long paymentId,
            @RequestBody(required = false) RefundPaymentRequest request) {
        java.math.BigDecimal amount = request != null ? request.getAmount() : null;
        String reason = request != null ? request.getReason() : null;
        Payment payment = paymentService.refundPayment(paymentId, amount, reason);
        return ResponseEntity.ok(ApiResponse.ok("Payment refunded successfully", payment));
    }
}
