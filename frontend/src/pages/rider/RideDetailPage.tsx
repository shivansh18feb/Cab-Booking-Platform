import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { rideService } from '../../services/rideService';

export default function RideDetailPage() {
  const { id } = useParams<{ id: string }>();

  const { data: ride, isLoading } = useQuery({
    queryKey: ['ride', id],
    queryFn: () => rideService.getRide(id!),
    enabled: !!id,
  });

  const receiptUrl = id ? rideService.getReceiptDownloadUrl(id) : '';

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

        {/* Fare Breakdown */}
        <div className="card">
          <h2 className="section-title mb-4">Fare Breakdown</h2>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between"><span className="text-gray-500">Base fare</span><span>\${(ride.baseFare ?? 0).toFixed(2)}</span></div>
            {(ride.discountAmount ?? 0) > 0 && (
              <div className="flex justify-between text-green-600"><span>Promo discount</span><span>-\${(ride.discountAmount ?? 0).toFixed(2)}</span></div>
            )}
            <div className="flex justify-between font-bold text-base border-t pt-2 mt-2">
              <span>Total Paid</span>
              <span className="text-indigo-600">\${(ride.finalFare ?? 0).toFixed(2)}</span>
            </div>
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
