package com.cab.booking.payment;

import com.cab.booking.common.ConflictException;
import com.cab.booking.config.AppProperties;
import com.cab.booking.driver.VehicleCategory;
import com.cab.booking.realtime.RealtimeNotificationService;
import com.cab.booking.ride.Ride;
import com.cab.booking.ride.RideEvent;
import com.cab.booking.ride.RideEventRepository;
import com.cab.booking.ride.RideRepository;
import com.cab.booking.ride.RideStatus;
import com.cab.booking.user.Role;
import com.cab.booking.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private PaymentService paymentService;

    private User rider;
    private Ride ride;
    private Payment payment;
    private AppProperties.Stripe stripeProps;

    @BeforeEach
    void setUp() {
        stripeProps = new AppProperties.Stripe();
        stripeProps.setWebhookSecret("whsec_dummy");
        stripeProps.setApiKey("sk_test_dummy");
        lenient().when(appProperties.getStripe()).thenReturn(stripeProps);

        rider = new User("Jane Rider", "jane@cabflow.app", "+14155551234", "pass", Role.RIDER);
        rider.setId(101L);

        ride = new Ride(
                rider,
                "Market St", 37.7749, -122.4194,
                "Mission St", 37.7600, -122.4200,
                VehicleCategory.SEDAN,
                new BigDecimal("3.20"), 12,
                new BigDecimal("15.50"), "USD"
        );
        ride.setId(500L);
        ride.setRideStatus(RideStatus.COMPLETED);

        payment = new Payment(ride, new BigDecimal("15.50"), "USD", "IDEMP-500-15.50");
        payment.setId(900L);
        payment.setProviderPaymentIntentId("pi_dev_test_123");
        payment.setPaymentStatus(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Should successfully process payment_intent.succeeded dev webhook")
    void shouldProcessPaymentSucceededWebhook() {
        when(paymentRepository.findByProviderPaymentIntentId("pi_dev_test_123"))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        String payload = """
                {
                  "type": "payment_intent.succeeded",
                  "data": {
                    "object": {
                      "id": "pi_dev_test_123",
                      "metadata": {
                        "rideId": "500"
                      }
                    }
                  }
                }
                """;

        paymentService.handleStripeWebhook(payload, null);

        assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus());
        assertNotNull(payment.getPaidAt());
        verify(rideEventRepository).save(any(RideEvent.class));
        verify(notificationService).notifyRideStatusUpdate(ride);
    }

    @Test
    @DisplayName("Should ignore duplicate payment_intent.succeeded webhook idempotently")
    void shouldIgnoreDuplicatePaymentWebhook() {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());

        when(paymentRepository.findByProviderPaymentIntentId("pi_dev_test_123"))
                .thenReturn(Optional.of(payment));

        String payload = """
                {
                  "type": "payment_intent.succeeded",
                  "data": {
                    "object": {
                      "id": "pi_dev_test_123"
                    }
                  }
                }
                """;

        paymentService.handleStripeWebhook(payload, null);

        // Should not save or notify again
        verify(paymentRepository, never()).save(any());
        verify(notificationService, never()).notifyRideStatusUpdate(any());
    }

    @Test
    @DisplayName("Should process payment_intent.payment_failed dev webhook")
    void shouldProcessPaymentFailedWebhook() {
        when(paymentRepository.findByProviderPaymentIntentId("pi_dev_test_123"))
                .thenReturn(Optional.of(payment));

        String payload = """
                {
                  "type": "payment_intent.payment_failed",
                  "data": {
                    "object": {
                      "id": "pi_dev_test_123"
                    }
                  }
                }
                """;

        paymentService.handleStripeWebhook(payload, null);

        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
        verify(paymentRepository).save(payment);
        verify(rideEventRepository).save(any(RideEvent.class));
    }

    @Test
    @DisplayName("Should successfully refund a completed payment")
    void shouldRefundCompletedPayment() {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());

        when(paymentRepository.findById(900L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment refunded = paymentService.refundPayment(900L, new BigDecimal("15.50"), "Trip dispute");

        assertEquals(PaymentStatus.REFUNDED, refunded.getPaymentStatus());
        assertEquals(new BigDecimal("15.50"), refunded.getRefundedAmount());
        verify(rideEventRepository).save(any(RideEvent.class));
    }

    @Test
    @DisplayName("Should reject refund on non-completed payment")
    void shouldRejectRefundOnPendingPayment() {
        payment.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(900L)).thenReturn(Optional.of(payment));

        assertThrows(ConflictException.class, () ->
                paymentService.refundPayment(900L, new BigDecimal("15.50"), "Invalid attempt"));
    }
}
