package com.cab.booking.payment;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.common.ConflictException;
import com.cab.booking.common.ResourceNotFoundException;
import com.cab.booking.config.AppProperties;
import com.cab.booking.email.EmailService;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.ride.*;
import com.cab.booking.security.UserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;
    private final RideEventRepository rideEventRepository;
    private final AppProperties appProperties;
    private final RealtimeNotificationService notificationService;
    private final RazorpayClientFactory razorpayClientFactory;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    public PaymentService(PaymentRepository paymentRepository,
                          RideRepository rideRepository,
                          RideEventRepository rideEventRepository,
                          AppProperties appProperties,
                          RealtimeNotificationService notificationService,
                          RazorpayClientFactory razorpayClientFactory,
                          ObjectMapper objectMapper,
                          EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.rideRepository = rideRepository;
        this.rideEventRepository = rideEventRepository;
        this.appProperties = appProperties;
        this.notificationService = notificationService;
        this.razorpayClientFactory = razorpayClientFactory;
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    /**
     * Initiates a Razorpay order for an eligible ride.
     * Calculates the authoritative amount from the database in INR (paise).
     */
    @Transactional
    public RazorpayOrderResponse createOrder(Long rideId, UserPrincipal principal) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        if (!ride.getRider().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new BadRequestException("Only the rider can initiate payment for this trip");
        }

        if (ride.getRideStatus() == RideStatus.CANCELLED) {
            throw new BadRequestException("Cannot initiate payment for a cancelled ride");
        }

        BigDecimal amount = ride.getFinalFare() != null ? ride.getFinalFare() : ride.getEstimatedFare();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid ride fare amount: " + amount);
        }

        long amountInPaise = amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
        String currency = appProperties.getRazorpay().getCurrency();
        if (currency == null || currency.isBlank()) {
            currency = "INR";
        }

        String idempotencyKey = "IDEMP-" + ride.getId() + "-" + amount.toPlainString();

        Payment payment = paymentRepository.findByRide(ride).orElseGet(() -> {
            Payment newPayment = new Payment(ride, amount, "INR", idempotencyKey);
            return paymentRepository.save(newPayment);
        });

        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new ConflictException("Payment for this ride is already completed");
        }

        String orderId;
        try {
            RazorpayClient razorpayClient = razorpayClientFactory.createClient();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "rcpt_" + ride.getId());

            JSONObject notes = new JSONObject();
            notes.put("rideId", String.valueOf(ride.getId()));
            notes.put("riderId", String.valueOf(ride.getRider().getId()));
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);
            orderId = order.get("id");
            log.info("Razorpay Order created: {} for ride #{}", orderId, ride.getId());
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception ex) {
            log.error("Failed to create Razorpay Order for ride {}: {}", ride.getId(), ex.getMessage(), ex);
            throw new BadRequestException("Failed to initiate Razorpay checkout: " + ex.getMessage());
        }

        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentProvider("RAZORPAY");
        payment.setProviderPaymentIntentId(orderId);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        RazorpayOrderResponse response = new RazorpayOrderResponse();
        response.setPaymentId(payment.getId());
        response.setRideId(ride.getId());
        response.setOrderId(orderId);
        response.setAmountInPaise(amountInPaise);
        response.setAmount(amount);
        response.setCurrency(currency);
        response.setKeyId(appProperties.getRazorpay().getKeyId());
        response.setStatus(payment.getPaymentStatus());
        response.setRiderName(ride.getRider().getFullName());
        response.setRiderEmail(ride.getRider().getEmail());
        response.setRiderPhone(ride.getRider().getPhoneNumber());

        return response;
    }

    /**
     * Verifies the checkout payment signature returned by the client.
     * Prevents client forgery using server-side HMAC-SHA256 signature verification.
     */
    @Transactional
    public Payment verifyPayment(VerifyPaymentRequest request, UserPrincipal principal) {
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + request.getRideId()));

        if (!ride.getRider().getId().equals(principal.getId()) && !principal.getRole().name().equals("ADMIN")) {
            throw new BadRequestException("Only the rider can verify payment for this trip");
        }

        Payment payment = paymentRepository.findByRide(ride)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ride: " + request.getRideId()));

        // Idempotency: If already completed, return successfully
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            log.info("Payment for ride {} is already COMPLETED (idempotent request)", ride.getId());
            return payment;
        }

        // Verify order ID match
        if (payment.getProviderPaymentIntentId() != null &&
                !payment.getProviderPaymentIntentId().equals(request.getRazorpayOrderId())) {
            throw new BadRequestException("Razorpay Order ID mismatch for ride " + ride.getId());
        }

        // Verify HMAC-SHA256 signature using the secret key
        String secret = appProperties.getRazorpay().getKeySecret();
        if (secret == null || secret.isBlank()) {
            throw new BadRequestException("Razorpay Key Secret is not configured on the server.");
        }

        boolean validSignature = RazorpaySignatureUtil.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                secret
        );

        if (!validSignature) {
            log.error("Invalid Razorpay payment signature for ride {}: orderId={}, paymentId={}",
                    ride.getId(), request.getRazorpayOrderId(), request.getRazorpayPaymentId());
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BadRequestException("Payment verification failed. Invalid digital signature.");
        }

        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());
        payment.setProviderPaymentId(request.getRazorpayPaymentId());
        payment.setProviderSignature(request.getRazorpaySignature());
        payment = paymentRepository.save(payment);

        RideEvent event = new RideEvent(
                ride,
                RideEventType.PAYMENT_COMPLETED.name(),
                ride.getRider(),
                "RIDER",
                "Payment of " + payment.getCurrency() + " " + payment.getAmount() +
                        " verified successfully via Razorpay (Payment ID: " + request.getRazorpayPaymentId() + ")"
        );
        rideEventRepository.save(event);

        notificationService.notifyRideStatusUpdate(ride);
        log.info("Payment successfully verified and completed for ride #{}", ride.getId());

        // Send confirmation email to rider (async, non-blocking)
        try {
            emailService.sendPaymentConfirmation(
                    ride.getRider().getEmail(),
                    ride.getRider().getFullName(),
                    ride.getId(),
                    payment.getAmount(),
                    request.getRazorpayPaymentId()
            );
        } catch (Exception e) {
            log.warn("Payment confirmation email could not be queued for ride #{}: {}", ride.getId(), e.getMessage());
        }

        return payment;
    }

    /**
     * Receives and processes Razorpay webhook events asynchronously.
     * Verifies the X-Razorpay-Signature header against the raw body.
     */
    @Transactional
    public void handleRazorpayWebhook(String rawPayload, String signatureHeader) {
        String webhookSecret = appProperties.getRazorpay().getWebhookSecret();

        if (webhookSecret != null && !webhookSecret.isBlank() && !webhookSecret.startsWith("dummy_")) {
            boolean valid = RazorpaySignatureUtil.verifyWebhookSignature(rawPayload, signatureHeader, webhookSecret);
            if (!valid) {
                log.error("Razorpay webhook signature verification failed");
                throw new BadRequestException("Invalid Razorpay webhook signature");
            }
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            String eventType = root.path("event").asText("");

            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String paymentId = paymentEntity.path("id").asText(null);
            String orderId = paymentEntity.path("order_id").asText(null);

            if (orderId == null || orderId.isBlank()) {
                orderId = root.path("payload").path("order").path("entity").path("id").asText(null);
            }

            String notesRideId = paymentEntity.path("notes").path("rideId").asText(null);
            Long rideId = null;
            if (notesRideId != null) {
                try { rideId = Long.parseLong(notesRideId); } catch (NumberFormatException ignored) {}
            }

            Payment payment = null;
            if (orderId != null) {
                payment = paymentRepository.findByProviderPaymentIntentId(orderId).orElse(null);
            }
            if (payment == null && paymentId != null) {
                payment = paymentRepository.findByProviderPaymentId(paymentId).orElse(null);
            }
            if (payment == null && rideId != null) {
                payment = paymentRepository.findByRideId(rideId).orElse(null);
            }

            if (payment == null) {
                log.warn("Webhook event '{}' received but no matching payment found for order: {}", eventType, orderId);
                return;
            }

            Ride ride = payment.getRide();

            switch (eventType) {
                case "payment.captured", "order.paid" -> {
                    if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                        log.info("Duplicate webhook: Payment for ride {} is already COMPLETED (idempotent)", ride.getId());
                        return;
                    }
                    payment.setPaymentStatus(PaymentStatus.COMPLETED);
                    payment.setPaidAt(Instant.now());
                    if (paymentId != null) {
                        payment.setProviderPaymentId(paymentId);
                    }
                    paymentRepository.save(payment);

                    RideEvent event = new RideEvent(
                            ride,
                            RideEventType.PAYMENT_COMPLETED.name(),
                            ride.getRider(),
                            "RAZORPAY_WEBHOOK",
                            "Payment confirmed via Razorpay webhook: " + paymentId
                    );
                    rideEventRepository.save(event);
                    notificationService.notifyRideStatusUpdate(ride);
                    log.info("Payment confirmed via Razorpay webhook for ride #{}", ride.getId());
                }

                case "payment.failed" -> {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);

                    RideEvent event = new RideEvent(
                            ride,
                            "PAYMENT_FAILED",
                            ride.getRider(),
                            "RAZORPAY_WEBHOOK",
                            "Razorpay payment failed for order: " + orderId
                    );
                    rideEventRepository.save(event);
                    log.warn("Payment marked FAILED via Razorpay webhook for ride #{}", ride.getId());
                }

                case "refund.processed" -> {
                    payment.setPaymentStatus(PaymentStatus.REFUNDED);
                    payment.setRefundedAmount(payment.getAmount());
                    paymentRepository.save(payment);

                    RideEvent event = new RideEvent(
                            ride,
                            "PAYMENT_REFUNDED",
                            ride.getRider(),
                            "RAZORPAY_WEBHOOK",
                            "Payment marked REFUNDED via Razorpay webhook"
                    );
                    rideEventRepository.save(event);
                    log.info("Payment marked REFUNDED via Razorpay webhook for ride #{}", ride.getId());
                }

                default -> log.info("Unhandled Razorpay webhook event type: {}", eventType);
            }

        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception ex) {
            log.error("Failed to parse Razorpay webhook payload: {}", ex.getMessage(), ex);
            throw new BadRequestException("Malformed Razorpay webhook payload: " + ex.getMessage());
        }
    }

    /**
     * Issues an administrative refund for a completed trip payment using Razorpay's API.
     */
    @Transactional
    public Payment refundPayment(Long paymentId, BigDecimal refundAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new ConflictException("Payment is already refunded");
        }

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new ConflictException("Cannot refund a payment with status: " + payment.getPaymentStatus());
        }

        BigDecimal toRefund = refundAmount != null ? refundAmount : payment.getAmount();
        long toRefundInPaise = toRefund.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();

        if (payment.getProviderPaymentId() != null && razorpayClientFactory.hasValidCredentials()) {
            try {
                RazorpayClient client = razorpayClientFactory.createClient();
                JSONObject refundRequest = new JSONObject();
                refundRequest.put("amount", toRefundInPaise);

                JSONObject notes = new JSONObject();
                notes.put("reason", reason != null ? reason : "Administrative refund");
                refundRequest.put("notes", notes);

                client.payments.refund(payment.getProviderPaymentId(), refundRequest);
                log.info("Razorpay refund API call succeeded for payment {}", payment.getProviderPaymentId());
            } catch (Exception ex) {
                log.error("Razorpay refund API invocation failed: {}", ex.getMessage(), ex);
                throw new BadRequestException("Razorpay refund failed: " + ex.getMessage());
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
                "Refund of " + payment.getCurrency() + " " + toRefund +
                        " processed. Reason: " + (reason != null ? reason : "Administrative refund")
        );
        rideEventRepository.save(event);

        log.info("Payment #{} successfully marked as REFUNDED for amount {}", paymentId, toRefund);
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentForRide(Long rideId, UserPrincipal principal) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found: " + rideId));

        if (principal != null && !principal.getRole().name().equals("ADMIN")) {
            boolean isRider = ride.getRider().getId().equals(principal.getId());
            boolean isDriver = ride.getDriver() != null && ride.getDriver().getUser().getId().equals(principal.getId());
            if (!isRider && !isDriver) {
                throw new org.springframework.security.access.AccessDeniedException("You do not have permission to view payment information for this ride");
            }
        }

        return paymentRepository.findByRideId(rideId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentForRide(Long rideId) {
        return paymentRepository.findByRideId(rideId).orElse(null);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PaymentResponse> getMyPayments(
            UserPrincipal principal, org.springframework.data.domain.Pageable pageable) {
        return paymentRepository.findByRiderId(principal.getId(), pageable)
                .map(PaymentResponse::fromEntity);
    }
}
