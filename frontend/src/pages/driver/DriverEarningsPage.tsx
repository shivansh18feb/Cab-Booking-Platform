import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { driverService } from '../../services/driverService';

export default function DriverEarningsPage() {
  const { data: earnings, isLoading } = useQuery({
    queryKey: ['driverEarnings'],
    queryFn: driverService.getEarnings,
  });

  return (
    <div className="max-w-3xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center gap-3 mb-6">
        <Link to="/driver/dashboard" className="text-gray-400 hover:text-gray-600">← Dashboard</Link>
        <h1 className="page-title">Earnings</h1>
      </div>

      {isLoading ? (
        <div className="space-y-4">
          {[...Array(4)].map((_, i) => <div key={i} className="skeleton h-24 rounded-xl" />)}
        </div>
      ) : (
        <div className="space-y-6">
          {/* Summary Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {[
              { label: 'Today',   value: earnings?.todayEarnings   ?? 0, icon: '📅', color: 'text-blue-600' },
              { label: 'This Week', value: earnings?.weekEarnings  ?? 0, icon: '📊', color: 'text-indigo-600' },
              { label: 'This Month', value: earnings?.monthEarnings ?? 0, icon: '💵', color: 'text-green-600' },
              { label: 'All Time', value: earnings?.totalEarnings  ?? 0, icon: '🏆', color: 'text-yellow-600' },
            ].map(card => (
              <div key={card.label} className="card text-center">
                <div className="text-3xl mb-2">{card.icon}</div>
                <p className={`text-2xl font-bold ${card.color}`}>\${card.value.toFixed(2)}</p>
                <p className="text-xs text-gray-400 mt-1">{card.label}</p>
              </div>
            ))}
          </div>

          {/* Trip Stats */}
          <div className="card">
            <h2 className="section-title mb-4">Trip Statistics</h2>
            <dl className="grid grid-cols-2 sm:grid-cols-3 gap-6">
              {[
                { dt: 'Total Trips',     dd: earnings?.totalRides ?? 0 },
                { dt: 'Trips Today',     dd: earnings?.todayRides ?? 0 },
                { dt: 'Trips This Week', dd: earnings?.weekRides  ?? 0 },
                { dt: 'Avg. Per Ride',   dd: `\$${(earnings?.avgPerRide ?? 0).toFixed(2)}` },
                { dt: 'Cancellations',   dd: earnings?.cancellations ?? 0 },
                { dt: 'Acceptance Rate', dd: `${earnings?.acceptanceRate ?? 0}%` },
              ].map(item => (
                <div key={item.dt}>
                  <dt className="text-xs text-gray-400">{item.dt}</dt>
                  <dd className="text-lg font-bold text-gray-900 mt-0.5">{item.dd}</dd>
                </div>
              ))}
            </dl>
          </div>

          {/* Recent Ride Earnings */}
          {earnings?.recentRides && earnings.recentRides.length > 0 && (
            <div className="card">
              <h2 className="section-title mb-4">Recent Rides</h2>
              <div className="divide-y divide-gray-100">
                {earnings.recentRides.map((ride: any) => (
                  <div key={ride.id} className="py-3 flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-900">{ride.pickupAddress?.slice(0, 35)}…</p>
                      <p className="text-xs text-gray-400">{new Date(ride.completedAt ?? ride.requestedAt).toLocaleDateString()}</p>
                    </div>
                    <span className="font-semibold text-green-600">+\${(ride.finalFare ?? 0).toFixed(2)}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
