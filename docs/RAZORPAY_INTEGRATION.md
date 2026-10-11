# Razorpay Payment Gateway Integration Guide

This document details the complete Razorpay payment integration for the Cab Booking Platform, replacing legacy Stripe components with a production-grade Indian Rupee (INR) Razorpay checkout architecture.

---

## 1. Architecture & Overview

### 1.1 Payment Flow Architecture
```
+-------------+                 +--------------------+                 +--------------------+
|   Rider     |                 |  Spring Boot API   |                 |      Razorpay      |
|  (Frontend) |                 |     (Backend)      |                 |    (Payment GW)    |
+------+------+                 +---------+----------+                 +---------+----------+
       |                                   |                                     |
       | 1. POST /payments/create-order    |                                     |
       |    { rideId: 500 }                |                                     |
       |---------------------------------->|                                     |
       |                                   | 2. Create Order (paise: ₹250 * 100) |
       |                                   |------------------------------------>|
       |                                   |<------------------------------------|
       |                                   |    Order ID: order_EKwxwAgItmmXdp   |
       |                                   |                                     |
       | 3. Order DTO + Razorpay Key ID    |                                     |
       |<----------------------------------|                                     |
       |                                                                         |
       | 4. Load checkout.js & Open Checkout Modal                               |
       |------------------------------------------------------------------------>|
       |                                                                         |
       | 5. Rider completes payment via Card / UPI / NetBanking / Wallet         |
       |<------------------------------------------------------------------------|
       |    razorpay_order_id, razorpay_payment_id, razorpay_signature           |
       |                                                                         |
       | 6. POST /payments/verify                                                |
       |    { rideId, razorpayOrderId, razorpayPaymentId, razorpaySignature }    |
       |---------------------------------->|                                     |
       |                                   | 7. HMAC-SHA256 Constant-Time Check  |
       |                                   |    (order_id + "|" + payment_id)    |
       |                                   |    against RAZORPAY_KEY_SECRET      |
       |                                   |                                     |
       |                                   | 8. Update Payment: COMPLETED        |
       |                                   |    Record paidAt, RideEvent, Notify |
       | 9. Payment Confirmation DTO       |                                     |
       |<----------------------------------|                                     |
       |                                   |                                     |
       |                                   | 10. Webhook (Async Confirmation)    |
       |                                   |<------------------------------------|
       |                                   |     X-Razorpay-Signature verified   |
       |                                   |     Idempotent duplicate check      |
```

---

## 2. Key Security Safeguards

1. **Server-Side Authoritative Pricing:**
   - Amount is strictly retrieved from the database (`ride.getFinalFare() != null ? ride.getFinalFare() : ride.getEstimatedFare()`).
   - The client cannot manipulate the charge amount.
2. **HMAC-SHA256 Digital Signature Verification:**
   - Client responses are never trusted on their own.
   - The backend validates `HMAC-SHA256(order_id + "|" + payment_id, key_secret)` using constant-time byte comparison (`MessageDigest.isEqual`) to defeat timing attacks.
3. **IDOR Prevention:**
   - `createOrder` and `verifyPayment` verify that the authenticated principal is either the ride's rider or an `ADMIN`.
4. **Idempotency & Replay Protection:**
   - Duplicate client verifications or duplicate webhook calls for an already `COMPLETED` payment safely return without re-charging or repeating status transitions.
5. **Fail-Safe Missing Credential Checks:**
   - If Razorpay credentials are not provided in production, clear configuration errors are returned instead of silent pseudo-success fallbacks.

---

## 3. Environment Variables

Configure these environment variables in your deployment environments:

### 3.1 Backend (Render)
| Environment Variable | Required | Description | Example Value |
|----------------------|----------|-------------|---------------|
| `RAZORPAY_KEY_ID` | Yes | Razorpay API Key ID | `rzp_test_...` or `rzp_live_...` |
| `RAZORPAY_KEY_SECRET` | Yes | Razorpay API Key Secret | `abc123xyz...` |
| `RAZORPAY_WEBHOOK_SECRET` | Recommended | Secret for Webhook Signature Verification | `whsec_my_secret_key` |
| `APP_CORS_ALLOWED_ORIGINS` | Yes | Frontend domain for CORS | `https://cab-booking-platform-seven.vercel.app,http://localhost:5173` |

### 3.2 Frontend (Vercel / Local)
| Environment Variable | Required | Description | Example Value |
|----------------------|----------|-------------|---------------|
| `VITE_API_BASE_URL` | Yes | Backend REST API Base URL | `https://cab-booking-platform-uq32.onrender.com/api/v1` |

---

## 4. Razorpay Dashboard Webhook Configuration

To receive real-time asynchronous payment updates:
1. Log in to [Razorpay Dashboard](https://dashboard.razorpay.com).
2. Go to **Settings** > **Webhooks**.
3. Click **Add New Webhook**.
4. Set **Webhook URL**:
   ```
   https://cab-booking-platform-uq32.onrender.com/api/v1/webhooks/razorpay
   ```
5. Set **Secret**: Enter a secure random string and set the same value in Render under `RAZORPAY_WEBHOOK_SECRET`.
6. Select the following **Active Events**:
   - `payment.captured`
   - `payment.failed`
   - `refund.processed`
7. Save the webhook.

---

## 5. API Endpoints Reference

### 5.1 Create Order
- **Endpoint:** `POST /api/v1/payments/create-order` (alias: `POST /api/v1/payments/intent`)
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Request Body:**
  ```json
  {
    "rideId": 500
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Razorpay order created successfully",
    "data": {
      "paymentId": 900,
      "rideId": 500,
      "orderId": "order_EKwxwAgItmmXdp",
      "amountInPaise": 25000,
      "amount": 250.00,
      "currency": "INR",
      "keyId": "rzp_test_...",
      "status": "PENDING",
      "riderName": "Jane Rider",
      "riderEmail": "jane@cabflow.app",
      "riderPhone": "+919876543210"
    }
  }
  ```

### 5.2 Verify Payment
- **Endpoint:** `POST /api/v1/payments/verify`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Request Body:**
  ```json
  {
    "rideId": 500,
    "razorpayOrderId": "order_EKwxwAgItmmXdp",
    "razorpayPaymentId": "pay_EKwyeuyxbhcmms",
    "razorpaySignature": "4c3b6f...a89"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Payment verified and completed successfully",
    "data": {
      "id": 900,
      "ride": { "id": 500 },
      "amount": 250.00,
      "currency": "INR",
      "paymentProvider": "RAZORPAY",
      "providerPaymentIntentId": "order_EKwxwAgItmmXdp",
      "providerPaymentId": "pay_EKwyeuyxbhcmms",
      "providerSignature": "4c3b6f...a89",
      "paymentStatus": "COMPLETED",
      "paidAt": "2026-10-11T22:30:00Z"
    }
  }
  ```

### 5.3 Webhook Handler
- **Endpoint:** `POST /api/v1/webhooks/razorpay` (legacy alias: `/api/v1/webhooks/stripe`)
- **Headers:** `X-Razorpay-Signature: <hmac_hex>`
- **Request Body:** Raw Razorpay webhook event payload

### 5.4 Administrative Refund
- **Endpoint:** `POST /api/v1/payments/{paymentId}/refund`
- **Headers:** `Authorization: Bearer <admin_jwt_token>`
- **Request Body:**
  ```json
  {
    "amount": 250.00,
    "reason": "Customer dispute resolution"
  }
  ```

---

## 6. Test Mode Credentials & Testing Checklist

### 6.1 Test Payment Cards (Razorpay Test Mode)
In Test Mode, you can test checkout using Razorpay standard test cards or UPI:
- **Card Number:** Any valid Luhn number, e.g. `4012 0000 0000 0002` (Visa), `5123 4567 8901 2346` (MasterCard)
- **Expiry:** Any date in the future (e.g. `12/32`)
- **CVV:** Any 3 digits (e.g. `123`)
- **OTP:** `123456` or click **Success** on the Razorpay mock OTP page.
- **UPI:** Enter any virtual address such as `success@razorpay` and select **Success**.

### 6.2 Testing Verification Checklist
- [x] Backend tests compile cleanly (`mvn test-compile`).
- [x] All 43 automated backend tests pass (`mvn test`).
- [x] 16 unit tests for Razorpay payment lifecycle, HMAC verification, IDOR prevention, webhooks, and refunds pass (`PaymentWebhookAndRefundTest`).
- [x] 6 unit tests for `RazorpaySignatureUtilTest` pass with constant-time verification.
- [x] Frontend TypeScript type check passes without errors (`npx tsc --noEmit`).
- [x] Frontend Vitest suite passes (`npm test -- --run`).
- [x] Frontend bundle build completes successfully (`npm run build`).
- [x] Vercel frontend CORS allowed in Spring Boot backend configuration.
- [x] Database migration `V4__razorpay_payments.sql` ready for automatic Flyway execution on startup.
