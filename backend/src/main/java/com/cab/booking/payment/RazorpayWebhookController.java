package com.cab.booking.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Razorpay webhook receiver with HMAC-SHA256 signature verification")
public class RazorpayWebhookController {

    private final PaymentService paymentService;

    public RazorpayWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping({"/razorpay", "/stripe"})
    @Operation(summary = "Receive Razorpay signed asynchronous webhook events")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signatureHeader,
            @RequestHeader(value = "Stripe-Signature", required = false) String fallbackHeader) {
        String effectiveSignature = (signatureHeader != null && !signatureHeader.isBlank())
                ? signatureHeader
                : fallbackHeader;
        paymentService.handleRazorpayWebhook(payload, effectiveSignature);
        return ResponseEntity.ok("OK");
    }
}
