import { apiClient } from './api';
import type {
  RazorpayOrderResponse,
  VerifyPaymentPayload,
  PaymentRecord,
} from '../types/payment';

declare global {
  interface Window {
    Razorpay?: any;
  }
}

/**
 * Dynamically loads the official Razorpay Checkout JavaScript SDK.
 */
export function loadRazorpayScript(): Promise<boolean> {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    script.onload = () => resolve(true);
    script.onerror = () => {
      console.error('Failed to load Razorpay Checkout script');
      resolve(false);
    };
    document.body.appendChild(script);
  });
}

export const paymentService = {
  /**
   * Request backend to create a server-side Razorpay Order for this ride.
   */
  async createOrder(rideId: number | string): Promise<RazorpayOrderResponse> {
    const res = await apiClient.post<{ data: RazorpayOrderResponse }>(
      '/payments/create-order',
      { rideId: Number(rideId) }
    );
    return res.data.data;
  },

  /**
   * Submit client payment confirmation and digital signature for backend verification.
   */
  async verifyPayment(payload: VerifyPaymentPayload): Promise<PaymentRecord> {
    const res = await apiClient.post<{ data: PaymentRecord }>(
      '/payments/verify',
      {
        rideId: Number(payload.rideId),
        razorpayOrderId: payload.razorpayOrderId,
        razorpayPaymentId: payload.razorpayPaymentId,
        razorpaySignature: payload.razorpaySignature,
      }
    );
    return res.data.data;
  },

  /**
   * Retrieve payment record and status for a specific ride.
   */
  async getPayment(rideId: number | string): Promise<PaymentRecord | null> {
    const res = await apiClient.get<{ data: PaymentRecord }>(
      `/payments/ride/${rideId}`
    );
    return res.data.data;
  },

  /**
   * Admin refund for a completed payment.
   */
  async refundPayment(
    paymentId: number | string,
    amount?: number,
    reason?: string
  ): Promise<PaymentRecord> {
    const res = await apiClient.post<{ data: PaymentRecord }>(
      `/payments/${paymentId}/refund`,
      { amount, reason }
    );
    return res.data.data;
  },

  /**
   * Retrieve paginated payment transactions for the authenticated rider.
   */
  async getMyPayments(
    page = 0,
    size = 10
  ): Promise<{ content: any[]; totalPages: number; totalElements: number }> {
    const res = await apiClient.get('/payments/my-payments', {
      params: { page, size },
    });
    return res.data.data;
  },

  /**
   * Download official PDF receipt as a Blob.
   */
  async downloadReceiptPdf(rideId: number | string): Promise<Blob> {
    const res = await apiClient.get(`/rides/${rideId}/receipt`, {
      responseType: 'blob',
    });
    return res.data;
  },

  /**
   * High-level flow: Creates order, loads SDK, and opens Razorpay modal with verification callback.
   */
  async initiateCheckout({
    rideId,
    onSuccess,
    onError,
    onDismiss,
  }: {
    rideId: number | string;
    onSuccess: (payment: PaymentRecord) => void;
    onError?: (error: any) => void;
    onDismiss?: () => void;
  }): Promise<void> {
    const scriptLoaded = await loadRazorpayScript();
    if (!scriptLoaded) {
      throw new Error('Could not load Razorpay payment SDK. Please check your internet connection.');
    }

    const orderData = await this.createOrder(rideId);

    const options = {
      key: orderData.keyId,
      amount: orderData.amountInPaise,
      currency: orderData.currency || 'INR',
      name: 'CabFlow Rides',
      description: `Payment for Ride #${rideId}`,
      order_id: orderData.orderId,
      prefill: {
        name: orderData.riderName || '',
        email: orderData.riderEmail || '',
        contact: orderData.riderPhone || '',
      },
      notes: {
        rideId: String(rideId),
      },
      theme: {
        color: '#4F46E5', // Indigo primary
      },
      handler: async (response: {
        razorpay_order_id: string;
        razorpay_payment_id: string;
        razorpay_signature: string;
      }) => {
        try {
          const verified = await paymentService.verifyPayment({
            rideId,
            razorpayOrderId: response.razorpay_order_id,
            razorpayPaymentId: response.razorpay_payment_id,
            razorpaySignature: response.razorpay_signature,
          });
          onSuccess(verified);
        } catch (err) {
          if (onError) onError(err);
        }
      },
      modal: {
        ondismiss: () => {
          if (onDismiss) onDismiss();
        },
      },
    };

    const rzpInstance = new window.Razorpay(options);
    rzpInstance.open();
  },
};

export default paymentService;
