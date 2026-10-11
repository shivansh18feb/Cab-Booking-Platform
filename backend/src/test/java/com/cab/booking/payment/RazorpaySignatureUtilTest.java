package com.cab.booking.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RazorpaySignatureUtilTest {

    private static final String SECRET = "test_razorpay_secret_12345";
    private static final String ORDER_ID = "order_O123456789";
    private static final String PAYMENT_ID = "pay_P987654321";

    @Test
    @DisplayName("Should successfully verify valid payment signature")
    void shouldVerifyValidPaymentSignature() {
        String data = ORDER_ID + "|" + PAYMENT_ID;
        String validSignature = RazorpaySignatureUtil.calculateSignature(data, SECRET);

        boolean result = RazorpaySignatureUtil.verifyPaymentSignature(ORDER_ID, PAYMENT_ID, validSignature, SECRET);
        assertTrue(result, "Valid signature should verify to true");
    }

    @Test
    @DisplayName("Should reject tampered payment signature")
    void shouldRejectTamperedPaymentSignature() {
        String data = ORDER_ID + "|" + PAYMENT_ID;
        String validSignature = RazorpaySignatureUtil.calculateSignature(data, SECRET);
        String tamperedSignature = validSignature.substring(0, validSignature.length() - 2) + "ab";

        boolean result = RazorpaySignatureUtil.verifyPaymentSignature(ORDER_ID, PAYMENT_ID, tamperedSignature, SECRET);
        assertFalse(result, "Tampered signature should fail verification");
    }

    @Test
    @DisplayName("Should reject payment signature when order ID is altered")
    void shouldRejectWhenOrderIdAltered() {
        String data = ORDER_ID + "|" + PAYMENT_ID;
        String validSignature = RazorpaySignatureUtil.calculateSignature(data, SECRET);

        boolean result = RazorpaySignatureUtil.verifyPaymentSignature("order_DIFFERENT", PAYMENT_ID, validSignature, SECRET);
        assertFalse(result, "Signature should fail if order ID does not match");
    }

    @Test
    @DisplayName("Should reject payment signature when payment ID is altered")
    void shouldRejectWhenPaymentIdAltered() {
        String data = ORDER_ID + "|" + PAYMENT_ID;
        String validSignature = RazorpaySignatureUtil.calculateSignature(data, SECRET);

        boolean result = RazorpaySignatureUtil.verifyPaymentSignature(ORDER_ID, "pay_DIFFERENT", validSignature, SECRET);
        assertFalse(result, "Signature should fail if payment ID does not match");
    }

    @Test
    @DisplayName("Should reject null or empty arguments safely")
    void shouldRejectNullOrEmptyArguments() {
        assertFalse(RazorpaySignatureUtil.verifyPaymentSignature(null, PAYMENT_ID, "sig", SECRET));
        assertFalse(RazorpaySignatureUtil.verifyPaymentSignature(ORDER_ID, null, "sig", SECRET));
        assertFalse(RazorpaySignatureUtil.verifyPaymentSignature(ORDER_ID, PAYMENT_ID, null, SECRET));
        assertFalse(RazorpaySignatureUtil.verifyPaymentSignature(ORDER_ID, PAYMENT_ID, "sig", null));
        assertFalse(RazorpaySignatureUtil.verifyWebhookSignature(null, "sig", SECRET));
        assertFalse(RazorpaySignatureUtil.verifyWebhookSignature("{}", null, SECRET));
        assertFalse(RazorpaySignatureUtil.verifyWebhookSignature("{}", "sig", null));
    }

    @Test
    @DisplayName("Should successfully verify valid webhook signature")
    void shouldVerifyValidWebhookSignature() {
        String payload = "{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_123\"}}}}";
        String validSig = RazorpaySignatureUtil.calculateSignature(payload, SECRET);

        boolean result = RazorpaySignatureUtil.verifyWebhookSignature(payload, validSig, SECRET);
        assertTrue(result, "Valid webhook signature should verify to true");
    }
}
