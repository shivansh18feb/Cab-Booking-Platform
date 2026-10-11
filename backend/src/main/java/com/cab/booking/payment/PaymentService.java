package com.cab.booking.payment;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.config.AppProperties;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.ride.*;
import com.cab.booking.security.UserPrincipal;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;
    private final RideEventRepository rideEventRepository;
    private final AppProperties appProperties;
    private final RealtimeNotificationService notificationService;

    public PaymentService(PaymentRepository paymentRepository,
                          RideRepository rideRepository,
                          RideEventRepository rideEventRepository,
                          AppProperties appProperties,
                          RealtimeNotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.rideRepository = rideRepository;
        this.rideEventRepository = rideEventRepository;
        this.appProperties = appProperties;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request, UserPrincipal principal) {
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + request.getRideId()));

        if (!ride.getRider().getId().equals(principal.getId())) {
            throw new BadRequestException("Only the rider can initiate payment for this trip");
        }

        BigDecimal amount = ride.getFinalFare() != null ? ride.getFinalFare() : ride.getEstimatedFare();
        String idempotencyKey = "IDEMP-" + ride.getId() + "-" + amount.toPlainString();

        Payment payment = paymentRepository.findByRide(ride).orElseGet(() -> {
            Payment newPayment = new Payment(ride, amount, ride.getCurrency(), idempotencyKey);
            return paymentRepository.save(newPayment);
        });

        String stripeApiKey = appProperties.getStripe().getApiKey();
        String clientSecret;
        String intentId;

        if (stripeApiKey != null && stripeApiKey.startsWith("sk_")) {
            try {
                Stripe.apiKey = stripeApiKey;
                long amountInCents = amount.multiply(new BigDecimal(100)).longValue();

                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency(ride.getCurrency().toLowerCase())
                        .setDescription("CabFlow Trip #" + ride.getId())
                        .putMetadata("rideId", String.valueOf(ride.getId()))
                        .putMetadata("riderId", String.valueOf(ride.getRider().getId()))
                        .build();

                PaymentIntent paymentIntent = PaymentIntent.create(params);
                clientSecret = paymentIntent.getClientSecret();
                intentId = paymentIntent.getId();
                log.info("Stripe PaymentIntent created: {} for ride {}", intentId, ride.getId());
            } catch (Exception ex) {
                log.error("Stripe API communication failed, utilizing sandbox simulation: {}", ex.getMessage());
                intentId = "pi_mock_" + UUID.randomUUID().toString().replace("-", "");
                clientSecret = intentId + "_secret_" + UUID.randomUUID().toString().substring(0, 8);
            }
        } else {
            // Documented Development Sandbox Mode
            log.info("STRIPE_API_KEY not provided or in dev mode. Generating deterministic test payment intent.");
            intentId = "pi_dev_sandbox_" + ride.getId();
            clientSecret = intentId + "_secret_test";
        }

        payment.setProviderPaymentIntentId(intentId);
        payment = paymentRepository.save(payment);

        PaymentIntentResponse response = new PaymentIntentResponse();
        response.setPaymentId(payment.getId());
        response.setRideId(ride.getId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentIntentId(intentId);
        response.setClientSecret(clientSecret);
        response.setStatus(payment.getPaymentStatus());

        return response;
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmationRequest request, UserPrincipal principal) {
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + request.getRideId()));

        Payment payment = paymentRepository.findByRide(ride)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ride: " + request.getRideId()));

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            return payment; // Already completed (idempotent)
        }

        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());
        payment = paymentRepository.save(payment);

        RideEvent event = new RideEvent(
                ride,
                RideEventType.PAYMENT_COMPLETED.name(),
                ride.getRider(),
                "RIDER",
                "Payment of " + payment.getCurrency() + " " + payment.getAmount() + " confirmed successfully"
        );
        rideEventRepository.save(event);

        notificationService.notifyRideStatusUpdate(ride);
        log.info("Payment confirmed successfully for ride {}", ride.getId());
        return payment;
    }

    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        String webhookSecret = appProperties.getStripe().getWebhookSecret();
        Event event;

        if (webhookSecret != null && !webhookSecret.isBlank() && !webhookSecret.startsWith("whsec_dummy")) {
            try {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } catch (Exception ex) {
                log.error("Stripe Webhook signature verification failed: {}", ex.getMessage());
                throw new BadRequestException("Invalid webhook signature: " + ex.getMessage());
            }
        } else {
            // Development / Test mode webhook parsing (use Jackson to extract fields; skip SDK sig-verify)
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(payload);
                String eventType = root.path("type").asText("");
                com.fasterxml.jackson.databind.JsonNode dataObj = root.path("data").path("object");
                String intentId2 = dataObj.path("id").asText(null);
                String metaRideId2 = dataObj.path("metadata").path("rideId").asText(null);
                Long rideId2 = null;
                if (metaRideId2 != null) {
                    try { rideId2 = Long.parseLong(metaRideId2); } catch (NumberFormatException ignored2) {}
                }
                handleParsedWebhookEvent(eventType, intentId2, rideId2);
                return;
            } catch (Exception ex) {
                log.error("Failed to parse Stripe webhook JSON: {}", ex.getMessage());
                throw new BadRequestException("Malformed webhook JSON: " + ex.getMessage());
            }
        }

        if (event == null) {
            throw new BadRequestException("Failed to construct Stripe webhook event");
        }

        String intentId = null;
        Long rideId = null;

        var dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            var stripeObject = dataObjectDeserializer.getObject().get();
            if (stripeObject instanceof PaymentIntent pi) {
                intentId = pi.getId();
                if (pi.getMetadata() != null && pi.getMetadata().containsKey("rideId")) {
                    try {
                        rideId = Long.parseLong(pi.getMetadata().get("rideId"));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Fallback JSON node extraction if SDK deserializer was not populated
        if (intentId == null) {
            try {
                com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
                com.fasterxml.jackson.databind.JsonNode dataNode = rootNode.path("data").path("object");
                if (!dataNode.isMissingNode()) {
                    intentId = dataNode.path("id").asText(null);
                    String metaRideId = dataNode.path("metadata").path("rideId").asText(null);
                    if (metaRideId != null) {
                        rideId = Long.parseLong(metaRideId);
                    }
                }
            } catch (Exception ignored) {}
        }

        Payment payment = null;
        if (intentId != null) {
            payment = paymentRepository.findByProviderPaymentIntentId(intentId).orElse(null);
        }
        if (payment == null && rideId != null) {
            payment = paymentRepository.findByRideId(rideId).orElse(null);
        }

        if (payment == null) {
            log.warn("Webhook event {} received but no associated payment record found for intent {}", event.getType(), intentId);
            return;
        }

        Ride ride = payment.getRide();

        switch (event.getType()) {
            case "payment_intent.succeeded":
                if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                    log.info("Duplicate webhook: Payment for ride {} is already COMPLETED (idempotent)", ride.getId());
                    return;
                }
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(Instant.now());
                paymentRepository.save(payment);

                RideEvent successEvent = new RideEvent(
                        ride,
                        RideEventType.PAYMENT_COMPLETED.name(),
                        ride.getRider(),
                        "STRIPE_WEBHOOK",
                        "Payment confirmed via Stripe webhook: " + intentId
                );
                rideEventRepository.save(successEvent);
                notificationService.notifyRideStatusUpdate(ride);
                log.info("Payment confirmed via Stripe webhook for ride {}", ride.getId());
                break;

            case "payment_intent.payment_failed":
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                RideEvent failEvent = new RideEvent(
                        ride,
                        "PAYMENT_FAILED",
                        ride.getRider(),
                        "STRIPE_WEBHOOK",
                        "Stripe payment intent failed: " + intentId
                );
                rideEventRepository.save(failEvent);
                log.warn("Payment failed via Stripe webhook for ride {}", ride.getId());
                break;

            case "charge.refunded":
            case "payment_intent.canceled":
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAmount(payment.getAmount());
                paymentRepository.save(payment);

                RideEvent refundEvent = new RideEvent(
                        ride,
                        "PAYMENT_REFUNDED",
                        ride.getRider(),
                        "STRIPE_WEBHOOK",
                        "Payment marked refunded via Stripe event: " + event.getType()
                );
                rideEventRepository.save(refundEvent);
                log.info("Payment marked refunded via Stripe webhook for ride {}", ride.getId());
                break;

            default:
                log.info("Unhandled Stripe webhook event type: {}", event.getType());
                break;
        }
    }

    @Transactional
    public Payment refundPayment(Long paymentId, BigDecimal refundAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new com.cab.booking.common.ConflictException("Payment is already refunded");
        }

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new com.cab.booking.common.ConflictException("Cannot refund a payment with status: " + payment.getPaymentStatus());
        }

        BigDecimal toRefund = refundAmount != null ? refundAmount : payment.getAmount();

        String stripeApiKey = appProperties.getStripe().getApiKey();
        if (stripeApiKey != null && stripeApiKey.startsWith("sk_") && payment.getProviderPaymentIntentId() != null && !payment.getProviderPaymentIntentId().startsWith("pi_dev_")) {
            try {
                Stripe.apiKey = stripeApiKey;
                com.stripe.param.RefundCreateParams params = com.stripe.param.RefundCreateParams.builder()
                        .setPaymentIntent(payment.getProviderPaymentIntentId())
                        .setAmount(toRefund.multiply(new BigDecimal(100)).longValue())
                        .setReason(com.stripe.param.RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                        .build();
                com.stripe.model.Refund.create(params);
            } catch (Exception ex) {
                log.error("Stripe refund call failed: {}", ex.getMessage());
                throw new BadRequestException("Stripe refund failed: " + ex.getMessage());
            }
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAmount(toRefund);
        payment = paymentRepository.save(payment);

        Ride ride = payment.getRide();
        RideEvent event = new RideEvent(
                ride,
                "PAYMENT_REFUNDED",
                ride.getRider(),
                "ADMIN",
                "Refund of " + payment.getCurrency() + " " + toRefund + " processed. Reason: " + (reason != null ? reason : "Administrative refund")
        );
        rideEventRepository.save(event);

        log.info("Payment #{} successfully refunded for amount {}", paymentId, toRefund);
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentForRide(Long rideId) {
        return paymentRepository.findByRideId(rideId).orElse(null);
    }

    @Transactional
    private void handleParsedWebhookEvent(String eventType, String intentId, Long rideId) {
        Payment payment = null;
        if (intentId != null) {
            payment = paymentRepository.findByProviderPaymentIntentId(intentId).orElse(null);
        }
        if (payment == null && rideId != null) {
            payment = paymentRepository.findByRideId(rideId).orElse(null);
        }
        if (payment == null) {
            log.warn("Dev webhook event {} received but no payment record found for intent {}", eventType, intentId);
            return;
        }
        Ride ride = payment.getRide();
        switch (eventType) {
            case "payment_intent.succeeded" -> {
                if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) return;
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(Instant.now());
                paymentRepository.save(payment);
                rideEventRepository.save(new RideEvent(ride, RideEventType.PAYMENT_COMPLETED.name(), ride.getRider(), "STRIPE_WEBHOOK", "Payment confirmed via webhook: " + intentId));
                notificationService.notifyRideStatusUpdate(ride);
                log.info("Payment confirmed via dev webhook for ride {}", ride.getId());
            }
            case "payment_intent.payment_failed" -> {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                rideEventRepository.save(new RideEvent(ride, "PAYMENT_FAILED", ride.getRider(), "STRIPE_WEBHOOK", "Payment failed: " + intentId));
                log.warn("Payment failed via dev webhook for ride {}", ride.getId());
            }
            case "charge.refunded", "payment_intent.canceled" -> {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAmount(payment.getAmount());
                paymentRepository.save(payment);
                rideEventRepository.save(new RideEvent(ride, "PAYMENT_REFUNDED", ride.getRider(), "STRIPE_WEBHOOK", "Refund via dev webhook event: " + eventType));
                log.info("Payment marked refunded via dev webhook for ride {}", ride.getId());
            }
            default -> log.info("Unhandled dev webhook event type: {}", eventType);
        }
    }
}
