export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export interface RazorpayOrderResponse {
  paymentId: number;
  rideId: number;
  orderId: string;
  amountInPaise: number;
  amount: number;
  currency: string;
  keyId: string;
  status: PaymentStatus;
  riderName?: string;
  riderEmail?: string;
  riderPhone?: string;
}

export interface VerifyPaymentPayload {
  rideId: number | string;
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}

export interface PaymentRecord {
  id: number;
  rideId?: number;
  amount: number;
  currency: string;
  paymentProvider: string;
  providerPaymentIntentId?: string;
  providerPaymentId?: string;
  providerSignature?: string;
  paymentStatus: PaymentStatus;
  paidAt?: string;
  refundedAmount?: number;
  createdAt?: string;
}

export interface PaymentTransaction {
  id: number;
  rideId?: number;
  pickupAddress?: string;
  dropoffAddress?: string;
  amount: number;
  currency: string;
  paymentProvider: string;
  paymentStatus: PaymentStatus;
  providerPaymentId?: string;
  providerPaymentIntentId?: string;
  paidAt?: string;
  refundedAmount?: number;
  createdAt: string;
}
