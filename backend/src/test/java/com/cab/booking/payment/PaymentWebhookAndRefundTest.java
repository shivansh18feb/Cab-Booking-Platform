package com.cab.booking.payment;

import com.cab.booking.common.BadRequestException;
import com.cab.booking.common.ConflictException;
import com.cab.booking.config.AppProperties;
import com.cab.booking.driver.VehicleCategory;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideEvent;
import com.cab.booking.ride.RideEventRepository;
import com.cab.booking.ride.RideRepository;
import com.cab.booking.ride.RideStatus;
import com.cab.booking.security.UserPrincipal;
import com.cab.booking.user.Role;
import com.cab.booking.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.PaymentClient;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookAndRefundTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RideRepository rideRepository;
    @Mock
    private RideEventRepository rideEventRepository;
    @Mock
    private AppProperties appProperties;
    @Mock
    private RealtimeNotificationService notificationService;
    @Mock
    private RazorpayClientFactory razorpayClientFactory;
    @Mock
    private com.cab.booking.email.EmailService emailService;

    private ObjectMapper objectMapper;
    private PaymentService paymentService;

    private User rider;
    private User otherUser;
    private UserPrincipal riderPrincipal;
    private UserPrincipal otherPrincipal;
    private Ride ride;
    private Payment payment;
    private AppProperties.Razorpay razorpayProps;

    private static final String TEST_KEY_ID = "rzp_test_key123";
    private static final String TEST_KEY_SECRET = "secret_key_abc_xyz_987";
    private static final String TEST_WEBHOOK_SECRET = "whsec_live_hook_pass_456";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        paymentService = new PaymentService(
                paymentRepository,
                rideRepository,
                rideEventRepository,
                appProperties,
                notificationService,
                razorpayClientFactory,
                objectMapper,
                emailService
        );

        razorpayProps = new AppProperties.Razorpay();
        razorpayProps.setKeyId(TEST_KEY_ID);
        razorpayProps.setKeySecret(TEST_KEY_SECRET);
        razorpayProps.setWebhookSecret(TEST_WEBHOOK_SECRET);
        razorpayProps.setCurrency("INR");
        lenient().when(appProperties.getRazorpay()).thenReturn(razorpayProps);

        rider = new User("Jane Rider", "jane@cabflow.app", "+919876543210", "pass", Role.RIDER);
        rider.setId(101L);
        riderPrincipal = UserPrincipal.create(rider);

        otherUser = new User("Bob Intruder", "bob@cabflow.app", "+919876543211", "pass", Role.RIDER);
        otherUser.setId(999L);
        otherPrincipal = UserPrincipal.create(otherUser);

        ride = new Ride(
                rider,
                "MG Road, Bengaluru", 12.9716, 77.5946,
                "Indiranagar, Bengaluru", 12.9784, 77.6408,
                VehicleCategory.SEDAN,
                new BigDecimal("5.50"), 18,
                new BigDecimal("250.00"), "INR"
        );
        ride.setId(500L);
        ride.setRideStatus(RideStatus.COMPLETED);

        payment = new Payment(ride, new BigDecimal("250.00"), "INR", "IDEMP-500-250.00");
        payment.setId(900L);
        payment.setPaymentProvider("RAZORPAY");
        payment.setProviderPaymentIntentId("order_test_order_123");
        payment.setPaymentStatus(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Should successfully create a Razorpay order for an eligible completed ride")
    void shouldCreateRazorpayOrderSuccessfully() throws Exception {
        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        RazorpayClient mockClient = mock(RazorpayClient.class);
        OrderClient mockOrderClient = mock(OrderClient.class);
        mockClient.orders = mockOrderClient;

        JSONObject orderJson = new JSONObject();
        orderJson.put("id", "order_test_order_123");
        Order mockOrder = new Order(orderJson);

        when(razorpayClientFactory.createClient()).thenReturn(mockClient);
        when(mockOrderClient.create(any(JSONObject.class))).thenReturn(mockOrder);

        RazorpayOrderResponse response = paymentService.createOrder(500L, riderPrincipal);

        assertNotNull(response);
        assertEquals("order_test_order_123", response.getOrderId());
        assertEquals(25000L, response.getAmountInPaise());
        assertEquals(new BigDecimal("250.00"), response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals(TEST_KEY_ID, response.getKeyId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals("Jane Rider", response.getRiderName());

        verify(paymentRepository, atLeastOnce()).save(payment);
    }

    @Test
    @DisplayName("Should reject order creation when user is not the ride's rider (IDOR prevention)")
    void shouldRejectOrderCreationForNonRider() {
        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                paymentService.createOrder(500L, otherPrincipal));
        assertTrue(ex.getMessage().contains("Only the rider can initiate payment"));
    }

    @Test
    @DisplayName("Should reject order creation if ride is cancelled")
    void shouldRejectOrderCreationForCancelledRide() {
        ride.setRideStatus(RideStatus.CANCELLED);
        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                paymentService.createOrder(500L, riderPrincipal));
        assertTrue(ex.getMessage().contains("Cannot initiate payment for a cancelled ride"));
    }

    @Test
    @DisplayName("Should reject order creation if payment is already completed")
    void shouldRejectOrderCreationIfAlreadyCompleted() {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.of(payment));

        ConflictException ex = assertThrows(ConflictException.class, () ->
                paymentService.createOrder(500L, riderPrincipal));
        assertTrue(ex.getMessage().contains("already completed"));
    }

    @Test
    @DisplayName("Should successfully verify payment signature and mark payment COMPLETED")
    void shouldVerifyPaymentSignatureSuccessfully() {
        String orderId = "order_test_order_123";
        String paymentId = "pay_test_pay_456";
        String validSignature = RazorpaySignatureUtil.calculateSignature(orderId + "|" + paymentId, TEST_KEY_SECRET);

        VerifyPaymentRequest request = new VerifyPaymentRequest(500L, orderId, paymentId, validSignature);

        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment verifiedPayment = paymentService.verifyPayment(request, riderPrincipal);

        assertEquals(PaymentStatus.COMPLETED, verifiedPayment.getPaymentStatus());
        assertNotNull(verifiedPayment.getPaidAt());
        assertEquals(paymentId, verifiedPayment.getProviderPaymentId());
        assertEquals(validSignature, verifiedPayment.getProviderSignature());

        verify(rideEventRepository).save(any(RideEvent.class));
        verify(notificationService).notifyRideStatusUpdate(ride);
    }

    @Test
    @DisplayName("Should reject payment verification when HMAC-SHA256 signature is forged or invalid")
    void shouldRejectPaymentVerificationWithInvalidSignature() {
        String orderId = "order_test_order_123";
        String paymentId = "pay_test_pay_456";
        String invalidSignature = "invalid_forged_signature_hex_value";

        VerifyPaymentRequest request = new VerifyPaymentRequest(500L, orderId, paymentId, invalidSignature);

        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.of(payment));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                paymentService.verifyPayment(request, riderPrincipal));

        assertTrue(ex.getMessage().contains("Invalid digital signature"));
        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
        verify(notificationService, never()).notifyRideStatusUpdate(any());
    }

    @Test
    @DisplayName("Should reject payment verification when order ID does not match database record")
    void shouldRejectPaymentVerificationOnOrderIdMismatch() {
        VerifyPaymentRequest request = new VerifyPaymentRequest(
                500L, "order_WRONG_MISMATCH_999", "pay_test_pay_456", "dummy_sig"
        );

        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.of(payment));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                paymentService.verifyPayment(request, riderPrincipal));

        assertTrue(ex.getMessage().contains("Order ID mismatch"));
    }

    @Test
    @DisplayName("Should return existing payment idempotently when verifyPayment is called on already completed payment")
    void shouldHandleVerifyPaymentIdempotency() {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());

        VerifyPaymentRequest request = new VerifyPaymentRequest(
                500L, "order_test_order_123", "pay_test_pay_456", "some_sig"
        );

        when(rideRepository.findById(500L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.of(payment));

        Payment result = paymentService.verifyPayment(request, riderPrincipal);

        assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
        verify(rideEventRepository, never()).save(any());
        verify(notificationService, never()).notifyRideStatusUpdate(any());
    }

    @Test
    @DisplayName("Should successfully process payment.captured Razorpay webhook")
    void shouldProcessPaymentCapturedWebhook() {
        when(paymentRepository.findByProviderPaymentIntentId("order_test_order_123"))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        String rawPayload = """
                {
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_webhook_999",
                        "order_id": "order_test_order_123",
                        "amount": 25000,
                        "currency": "INR",
                        "status": "captured",
                        "notes": {
                          "rideId": "500"
                        }
                      }
                    }
                  }
                }
                """;

        String signature = RazorpaySignatureUtil.calculateSignature(rawPayload, TEST_WEBHOOK_SECRET);

        paymentService.handleRazorpayWebhook(rawPayload, signature);

        assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus());
        assertNotNull(payment.getPaidAt());
        assertEquals("pay_webhook_999", payment.getProviderPaymentId());
        verify(rideEventRepository).save(any(RideEvent.class));
        verify(notificationService).notifyRideStatusUpdate(ride);
    }

    @Test
    @DisplayName("Should ignore duplicate payment.captured webhook idempotently")
    void shouldIgnoreDuplicatePaymentCapturedWebhook() {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());

        when(paymentRepository.findByProviderPaymentIntentId("order_test_order_123"))
                .thenReturn(Optional.of(payment));

        String rawPayload = """
                {
                  "event": "payment.captured",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_webhook_999",
                        "order_id": "order_test_order_123"
                      }
                    }
                  }
                }
                """;

        String signature = RazorpaySignatureUtil.calculateSignature(rawPayload, TEST_WEBHOOK_SECRET);

        paymentService.handleRazorpayWebhook(rawPayload, signature);

        verify(paymentRepository, never()).save(any());
        verify(notificationService, never()).notifyRideStatusUpdate(any());
    }

    @Test
    @DisplayName("Should reject webhook with invalid signature")
    void shouldRejectWebhookWithInvalidSignature() {
        String rawPayload = "{\"event\":\"payment.captured\"}";
        String invalidSignature = "invalid_webhook_signature";

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                paymentService.handleRazorpayWebhook(rawPayload, invalidSignature));

        assertTrue(ex.getMessage().contains("Invalid Razorpay webhook signature"));
    }

    @Test
    @DisplayName("Should process payment.failed Razorpay webhook")
    void shouldProcessPaymentFailedWebhook() {
        when(paymentRepository.findByProviderPaymentIntentId("order_test_order_123"))
                .thenReturn(Optional.of(payment));

        String rawPayload = """
                {
                  "event": "payment.failed",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_failed_888",
                        "order_id": "order_test_order_123"
                      }
                    }
                  }
                }
                """;

        String signature = RazorpaySignatureUtil.calculateSignature(rawPayload, TEST_WEBHOOK_SECRET);

        paymentService.handleRazorpayWebhook(rawPayload, signature);

        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
        verify(rideEventRepository).save(any(RideEvent.class));
    }

    @Test
    @DisplayName("Should process refund.processed Razorpay webhook")
    void shouldProcessRefundProcessedWebhook() {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByProviderPaymentIntentId("order_test_order_123"))
                .thenReturn(Optional.of(payment));

        String rawPayload = """
                {
                  "event": "refund.processed",
                  "payload": {
                    "payment": {
                      "entity": {
                        "id": "pay_test_pay_456",
                        "order_id": "order_test_order_123"
                      }
                    }
                  }
                }
                """;

        String signature = RazorpaySignatureUtil.calculateSignature(rawPayload, TEST_WEBHOOK_SECRET);

        paymentService.handleRazorpayWebhook(rawPayload, signature);

        assertEquals(PaymentStatus.REFUNDED, payment.getPaymentStatus());
        assertEquals(new BigDecimal("250.00"), payment.getRefundedAmount());
        verify(paymentRepository).save(payment);
        verify(rideEventRepository).save(any(RideEvent.class));
    }

    @Test
    @DisplayName("Should successfully refund a completed payment via admin service")
    void shouldRefundCompletedPayment() throws Exception {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());
        payment.setProviderPaymentId("pay_live_test_777");

        when(paymentRepository.findById(900L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(razorpayClientFactory.hasValidCredentials()).thenReturn(true);

        RazorpayClient mockClient = mock(RazorpayClient.class);
        PaymentClient mockPaymentClient = mock(PaymentClient.class);
        mockClient.payments = mockPaymentClient;
        when(razorpayClientFactory.createClient()).thenReturn(mockClient);

        Payment refunded = paymentService.refundPayment(900L, new BigDecimal("250.00"), "Rider dispute approved");

        assertEquals(PaymentStatus.REFUNDED, refunded.getPaymentStatus());
        assertEquals(new BigDecimal("250.00"), refunded.getRefundedAmount());
        verify(mockPaymentClient).refund(eq("pay_live_test_777"), any(JSONObject.class));
        verify(rideEventRepository).save(any(RideEvent.class));
    }

    @Test
    @DisplayName("Should reject refund on pending payment")
    void shouldRejectRefundOnPendingPayment() {
        payment.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(900L)).thenReturn(Optional.of(payment));

        assertThrows(ConflictException.class, () ->
                paymentService.refundPayment(900L, new BigDecimal("250.00"), "Dispute"));
    }

    @Test
    @DisplayName("Should reject refund on already refunded payment")
    void shouldRejectRefundOnAlreadyRefundedPayment() {
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findById(900L)).thenReturn(Optional.of(payment));

        assertThrows(ConflictException.class, () ->
                paymentService.refundPayment(900L, new BigDecimal("250.00"), "Dispute"));
    }
}
