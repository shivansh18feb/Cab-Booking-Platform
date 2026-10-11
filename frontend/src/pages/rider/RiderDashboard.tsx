import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { rideService } from '../../services/rideService';
import { formatCurrency } from '../../utils/formatters';

const statusColor: Record<string, string> = {
  REQUESTED:   'bg-yellow-100 text-yellow-800',
  ACCEPTED:    'bg-blue-100 text-blue-800',
  DRIVER_ARRIVING: 'bg-blue-100 text-blue-800',
  IN_PROGRESS: 'bg-indigo-100 text-indigo-800',
  COMPLETED:   'bg-green-100 text-green-800',
  CANCELLED:   'bg-red-100 text-red-800',
};

export default function RiderDashboard() {
  const { user } = useAuth();

  const { data: activeRide, isLoading: loadingActive } = useQuery({
    queryKey: ['activeRide'],
    queryFn: rideService.getActiveRide,
    refetchInterval: 10_000,
  });

  const { data: history } = useQuery({
    queryKey: ['rideHistory', 0],
    queryFn: () => rideService.getRideHistory(0, 5),
  });

  return (
    <div className="max-w-4xl mx-auto px-4 py-8 space-y-6 fade-in">
      {/* Welcome */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Welcome back, {user?.fullName?.split(' ')[0]}! 👋</h1>
          <p className="text-gray-500 mt-1">Where are you heading today?</p>
        </div>
        <Link to="/rider/book" className="btn-primary flex items-center gap-2">
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Book a Ride
        </Link>
      </div>

      {/* Active Ride Banner */}
      {loadingActive ? (
        <div className="card skeleton h-32" />
      ) : activeRide ? (
        <div className="card border-l-4 border-indigo-500 bg-indigo-50">
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <span className="live-pulse w-2 h-2 bg-indigo-500 rounded-full inline-block" />
                <span className="text-xs font-semibold text-indigo-600 uppercase tracking-wide">Active Ride</span>
              </div>
              <p className="font-semibold text-gray-900">{activeRide.pickupAddress} → {activeRide.dropoffAddress}</p>
              <span className={`status-badge ${statusColor[activeRide.status] ?? 'bg-gray-100 text-gray-700'}`}>
                {activeRide.status.replace(/_/g, ' ')}
              </span>
            </div>
            <Link to={`/rider/ride/${activeRide.id}/track`} className="btn-primary shrink-0">
              Track Ride
            </Link>
          </div>
        </div>
      ) : (
        <div className="card bg-gradient-to-br from-indigo-600 to-purple-700 text-white">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-bold mb-1">Ready to go?</h2>
              <p className="text-indigo-100 text-sm">Book a ride in seconds. Fast, safe, reliable.</p>
            </div>
            <span className="text-6xl">🚖</span>
          </div>
          <Link
            to="/rider/book"
            className="mt-4 inline-block bg-white text-indigo-700 font-semibold px-6 py-2.5 rounded-lg hover:bg-indigo-50 transition-colors"
          >
            Book Now
          </Link>
        </div>
      )}

      {/* Quick Actions */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {[
          { to: '/rider/book',         icon: '🚗', label: 'Book Ride' },
          { to: '/rider/history',      icon: '📋', label: 'Ride History' },
          { to: '/rider/promotions',   icon: '🎁', label: 'Promotions' },
          { to: '/rider/saved-places', icon: '📍', label: 'Saved Places' },
        ].map(item => (
          <Link key={item.to} to={item.to} className="card-hover text-center hover:border-indigo-200">
            <div className="text-3xl mb-2">{item.icon}</div>
            <p className="text-sm font-medium text-gray-700">{item.label}</p>
          </Link>
        ))}
      </div>

      {/* Recent Rides */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h2 className="section-title">Recent Rides</h2>
          <Link to="/rider/history" className="text-sm text-indigo-600 hover:underline">View all</Link>
        </div>
        {!history || history.content.length === 0 ? (
          <div className="text-center py-10 text-gray-400">
            <div className="text-4xl mb-2">🛣️</div>
            <p>No rides yet. Book your first ride!</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {history.content.map(ride => (
              <Link key={ride.id} to={`/rider/ride/${ride.id}`} className="flex items-center justify-between py-3 hover:bg-gray-50 px-2 -mx-2 rounded-lg transition-colors">
                <div className="space-y-0.5">
                  <p className="text-sm font-medium text-gray-900">{ride.pickupAddress} → {ride.dropoffAddress}</p>
                  <p className="text-xs text-gray-400">{new Date(ride.requestedAt).toLocaleDateString()}</p>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-semibold text-gray-900">{formatCurrency(ride.finalFare, ride.currency)}</p>
                  <span className={`status-badge ${statusColor[ride.status] ?? ''}`}>{ride.status}</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
