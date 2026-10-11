import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { rideService } from '../../services/rideService';
import { paymentService } from '../../services/paymentService';
import { formatCurrency } from '../../utils/formatters';

export default function RideDetailPage() {
  const { id } = useParams<{ id: string }>();
  const qc = useQueryClient();
  const [isPaying, setIsPaying] = useState(false);
  const [payError, setPayError] = useState<string | null>(null);

  const { data: ride, isLoading } = useQuery({
    queryKey: ['ride', id],
    queryFn: () => rideService.getRide(id!),
    enabled: !!id,
  });

  const { data: payment, refetch: refetchPayment } = useQuery({
    queryKey: ['payment', id],
    queryFn: () => paymentService.getPayment(id!),
    enabled: !!id,
  });

  const receiptUrl = id ? rideService.getReceiptDownloadUrl(id) : '';

  const handlePayNow = async () => {
    if (!id) return;
    setIsPaying(true);
    setPayError(null);
    try {
      await paymentService.initiateCheckout({
        rideId: id,
        onSuccess: (verifiedPayment) => {
          setIsPaying(false);
          qc.setQueryData(['payment', id], verifiedPayment);
          qc.invalidateQueries({ queryKey: ['ride', id] });
          refetchPayment();
        },
        onError: (err: any) => {
          setIsPaying(false);
          const msg =
            err?.response?.data?.message ||
            err?.message ||
            'Payment verification failed. Please try again.';
          setPayError(msg);
        },
        onDismiss: () => {
          setIsPaying(false);
        },
      });
    } catch (err: any) {
      setIsPaying(false);
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        'Unable to initialize Razorpay checkout.';
      setPayError(msg);
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8">
        <div className="skeleton h-8 w-48 mb-6" />
        <div className="card skeleton h-64" />
      </div>
    );
  }

  if (!ride) return (
    <div className="max-w-2xl mx-auto px-4 py-8 text-center">
      <p className="text-gray-500">Ride not found.</p>
      <Link to="/rider/history" className="btn-primary mt-4 inline-block">Back to History</Link>
    </div>
  );

  const isPaid = payment?.paymentStatus === 'COMPLETED';
  const isRefunded = payment?.paymentStatus === 'REFUNDED';

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center gap-3 mb-6">
        <Link to="/rider/history" className="text-gray-400 hover:text-gray-600">← History</Link>
        <h1 className="page-title">Ride Details</h1>
      </div>

      <div className="space-y-4">
        {/* Route */}
        <div className="card">
          <h2 className="section-title mb-4">Route</h2>
          <div className="space-y-3">
            <div className="flex items-start gap-3">
              <span className="w-7 h-7 bg-green-500 text-white rounded-full flex items-center justify-center text-xs font-bold shrink-0">P</span>
              <div>
                <p className="text-xs text-gray-400">Pickup</p>
                <p className="text-sm font-medium">{ride.pickupAddress}</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <span className="w-7 h-7 bg-red-500 text-white rounded-full flex items-center justify-center text-xs font-bold shrink-0">D</span>
              <div>
                <p className="text-xs text-gray-400">Drop-off</p>
                <p className="text-sm font-medium">{ride.dropoffAddress}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Details Grid */}
        <div className="card">
          <h2 className="section-title mb-4">Trip Details</h2>
          <dl className="grid grid-cols-2 gap-4">
            {[
              { dt: 'Status',         dd: ride.status.replace(/_/g, ' ') },
              { dt: 'Vehicle',        dd: ride.vehicleCategory },
              { dt: 'Distance',       dd: ride.distanceKm ? `${ride.distanceKm.toFixed(1)} km` : '—' },
              { dt: 'Duration',       dd: ride.durationMin ? `${ride.durationMin} min` : '—' },
              { dt: 'Requested',      dd: new Date(ride.requestedAt).toLocaleString() },
              { dt: 'Completed',      dd: ride.completedAt ? new Date(ride.completedAt).toLocaleString() : '—' },
            ].map(item => (
              <div key={item.dt}>
                <dt className="text-xs text-gray-400">{item.dt}</dt>
                <dd className="text-sm font-medium text-gray-900 mt-0.5">{item.dd}</dd>
              </div>
            ))}
          </dl>
        </div>

        {/* Fare & Payment Breakdown */}
        <div className="card">
          <h2 className="section-title mb-4">Fare & Payment Breakdown</h2>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-500">Base fare</span>
              <span>{formatCurrency(ride.baseFare ?? 0, ride.currency)}</span>
            </div>
            {(ride.discountAmount ?? 0) > 0 && (
              <div className="flex justify-between text-green-600">
                <span>Promo discount</span>
                <span>-{formatCurrency(ride.discountAmount ?? 0, ride.currency)}</span>
              </div>
            )}
            <div className="flex justify-between font-bold text-base border-t pt-2 mt-2">
              <span>Total Fare</span>
              <span className="text-indigo-600">
                {formatCurrency(ride.finalFare ?? ride.estimatedFare ?? 0, ride.currency)}
              </span>
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-gray-100">
            {isPaid ? (
              <div className="p-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-700">
                <div className="flex items-center justify-between font-semibold">
                  <span>✅ Paid via Razorpay</span>
                  {payment?.providerPaymentId && (
                    <span className="text-xs font-mono text-green-600">
                      {payment.providerPaymentId}
                    </span>
                  )}
                </div>
                <p className="text-xs text-green-600 mt-0.5">
                  Confirmed on {payment?.paidAt ? new Date(payment.paidAt).toLocaleString() : 'Trip completion'}
                </p>
              </div>
            ) : isRefunded ? (
              <div className="p-3 bg-purple-50 border border-purple-200 rounded-lg text-sm text-purple-700">
                <div className="flex items-center justify-between font-semibold">
                  <span>↩️ Refunded via Razorpay</span>
                  <span>{formatCurrency(payment?.refundedAmount ?? ride.finalFare ?? 0, ride.currency)}</span>
                </div>
              </div>
            ) : ride.status === 'COMPLETED' ? (
              <div className="space-y-2">
                <button
                  onClick={handlePayNow}
                  disabled={isPaying}
                  className="w-full py-2.5 px-4 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-lg shadow transition flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {isPaying ? (
                    <>
                      <span className="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent" />
                      Opening Razorpay…
                    </>
                  ) : (
                    <>
                      <span>💳</span>
                      Pay {formatCurrency(ride.finalFare ?? ride.estimatedFare ?? 0, ride.currency)} with Razorpay
                    </>
                  )}
                </button>
                {payError && (
                  <p className="text-xs text-red-600 bg-red-50 p-2 rounded border border-red-200">
                    {payError}
                  </p>
                )}
              </div>
            ) : null}
          </div>
        </div>

        {/* Actions */}
        {ride.status === 'COMPLETED' && (
          <div className="flex gap-3">
            <a
              href={receiptUrl}
              target="_blank"
              rel="noreferrer"
              className="btn-secondary flex-1 text-center flex items-center justify-center gap-2"
            >
              📄 Download Receipt
            </a>
            <Link to="/rider/book" className="btn-primary flex-1 text-center">Book Again</Link>
          </div>
        )}
      </div>
    </div>
  );
}
