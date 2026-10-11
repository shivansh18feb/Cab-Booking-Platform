import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { driverService } from '../../services/driverService';
import { useAuth } from '../../context/AuthContext';

export default function DriverProfilePage() {
  const { user } = useAuth();
  const { data: profile, isLoading } = useQuery({
    queryKey: ['driverProfile'],
    queryFn: driverService.getProfile,
  });

  if (isLoading) return <div className="max-w-2xl mx-auto px-4 py-8"><div className="skeleton h-64 rounded-xl" /></div>;

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 fade-in">
      <div className="flex items-center gap-3 mb-6">
        <Link to="/driver/dashboard" className="text-gray-400 hover:text-gray-600">← Dashboard</Link>
        <h1 className="page-title">My Profile</h1>
      </div>

      <div className="space-y-4">
        {/* Avatar & Basic */}
        <div className="card flex items-center gap-5">
          <div className="w-20 h-20 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-full flex items-center justify-center text-white text-3xl font-bold shrink-0">
            {user?.fullName?.[0]?.toUpperCase() ?? 'D'}
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900">{user?.fullName}</h2>
            <p className="text-gray-500">{user?.email}</p>
            <div className="flex items-center gap-2 mt-2">
              <span className={`status-badge ${profile?.verificationStatus === 'APPROVED' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'}`}>
                {profile?.verificationStatus === 'APPROVED' ? '✅ Verified' : '⏳ Pending Verification'}
              </span>
            </div>
          </div>
        </div>

        {/* Vehicle Details */}
        {profile?.vehicle && (
          <div className="card">
            <h2 className="section-title mb-4">Vehicle</h2>
            <dl className="grid grid-cols-2 gap-4 text-sm">
              {[
                { dt: 'Type',         dd: profile.vehicle.category },
                { dt: 'Make & Model', dd: `${profile.vehicle.make} ${profile.vehicle.model}` },
                { dt: 'Year',         dd: profile.vehicle.year },
                { dt: 'Color',        dd: profile.vehicle.color },
                { dt: 'Registration', dd: profile.vehicle.registrationNumber },
                { dt: 'Capacity',     dd: `${profile.vehicle.seatingCapacity} seats` },
              ].map(item => (
                <div key={item.dt}>
                  <dt className="text-xs text-gray-400">{item.dt}</dt>
                  <dd className="font-medium text-gray-900 mt-0.5">{item.dd}</dd>
                </div>
              ))}
            </dl>
          </div>
        )}

        {/* Performance */}
        <div className="card">
          <h2 className="section-title mb-4">Performance</h2>
          <dl className="grid grid-cols-3 gap-4 text-center">
            <div>
              <dt className="text-xs text-gray-400">Rating</dt>
              <dd className="text-2xl font-bold text-yellow-500">⭐ {profile?.averageRating?.toFixed(1) ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-xs text-gray-400">Total Rides</dt>
              <dd className="text-2xl font-bold text-indigo-600">{profile?.totalRides ?? 0}</dd>
            </div>
            <div>
              <dt className="text-xs text-gray-400">License #</dt>
              <dd className="text-sm font-medium text-gray-700 mt-1">{profile?.licenseNumber ?? '—'}</dd>
            </div>
          </dl>
        </div>
      </div>
    </div>
  );
}
