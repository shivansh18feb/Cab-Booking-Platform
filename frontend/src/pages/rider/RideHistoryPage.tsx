import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { rideService } from '../../services/rideService';
import { formatCurrency } from '../../utils/formatters';

const statusColor: Record<string, string> = {
  REQUESTED: 'bg-yellow-100 text-yellow-800',
  ACCEPTED: 'bg-blue-100 text-blue-800',
  IN_PROGRESS: 'bg-indigo-100 text-indigo-800',
  COMPLETED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
};

export default function RideHistoryPage() {
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['rideHistory', page],
    queryFn: () => rideService.getRideHistory(page, 10),
  });

  return (
    <div className="max-w-3xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">Ride History</h1>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[...Array(5)].map((_, i) => <div key={i} className="skeleton h-20 rounded-xl" />)}
        </div>
      ) : !data || data.content.length === 0 ? (
        <div className="card text-center py-16">
          <div className="text-5xl mb-3">🛣️</div>
          <p className="text-gray-500">No rides yet. Book your first ride!</p>
          <Link to="/rider/book" className="btn-primary mt-4 inline-block">Book a Ride</Link>
        </div>
      ) : (
        <>
          <div className="space-y-3">
            {data.content.map(ride => (
              <Link
                key={ride.id}
                to={`/rider/ride/${ride.id}`}
                className="card-hover flex items-center justify-between gap-4"
              >
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 bg-indigo-100 rounded-xl flex items-center justify-center text-xl shrink-0">
                    {ride.vehicleCategory === 'PREMIUM' ? '🏎️' : ride.vehicleCategory === 'SUV' ? '🚙' : '🚗'}
                  </div>
                  <div>
                    <p className="font-medium text-gray-900 text-sm">{ride.pickupAddress?.slice(0, 40)}…</p>
                    <p className="text-xs text-gray-400">→ {ride.dropoffAddress?.slice(0, 40)}…</p>
                    <p className="text-xs text-gray-400 mt-0.5">{new Date(ride.requestedAt).toLocaleDateString(undefined, { dateStyle: 'medium' })}</p>
                  </div>
                </div>
                <div className="text-right shrink-0">
                  <p className="font-bold text-gray-900">{formatCurrency(ride.finalFare ?? 0, ride.currency)}</p>
                  <span className={`status-badge mt-1 ${statusColor[ride.status] ?? 'bg-gray-100 text-gray-600'}`}>
                    {ride.status.replace(/_/g, ' ')}
                  </span>
                </div>
              </Link>
            ))}
          </div>

          {/* Pagination */}
          {(data.totalPages ?? 1) > 1 && (
            <div className="flex items-center justify-center gap-3 mt-6">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="btn-secondary text-sm px-3 py-1.5">← Prev</button>
              <span className="text-sm text-gray-500">Page {page + 1} of {data.totalPages}</span>
              <button disabled={page + 1 >= (data.totalPages ?? 1)} onClick={() => setPage(p => p + 1)} className="btn-secondary text-sm px-3 py-1.5">Next →</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
